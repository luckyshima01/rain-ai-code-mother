import { ref } from 'vue'
import type { Ref } from 'vue'

export interface SelectedElementInfo {
  tagName: string
  id: string | null
  classes: string | null
  text: string | null
  xpath: string
}

/**
 * Script injected directly into the iframe's document.
 * Uses a guard flag so repeated injections (e.g. on iframe reload) are idempotent.
 */
const EDITOR_SCRIPT = `
(function () {
  if (window.__rainVisualEditor) return;
  window.__rainVisualEditor = true;

  var _hovered = null;
  var _selected = null;

  function getXPath(el) {
    var parts = [];
    var node = el;
    while (node && node.nodeType === 1 && node.tagName !== 'HTML') {
      var idx = 1;
      var sib = node.previousElementSibling;
      while (sib) { if (sib.tagName === node.tagName) idx++; sib = sib.previousElementSibling; }
      parts.unshift(node.tagName.toLowerCase() + '[' + idx + ']');
      node = node.parentElement;
    }
    return '/html/' + parts.join('/');
  }

  function getInfo(el) {
    var cls = (typeof el.className === 'string') ? el.className.trim() : '';
    return {
      tagName: el.tagName.toLowerCase(),
      id: el.id || null,
      classes: cls || null,
      text: (el.innerText || '').replace(/\\s+/g, ' ').trim().slice(0, 120) || null,
      xpath: getXPath(el)
    };
  }

  function applyHover(el) {
    if (!el.__rainSavedOutline) {
      el.__rainSavedOutline = el.style.outline || '';
      el.__rainSavedOffset = el.style.outlineOffset || '';
    }
    el.style.outline = '2px dashed #1677ff';
    el.style.outlineOffset = '-2px';
  }

  function applySelected(el) {
    el.style.outline = '2px solid #1677ff';
    el.style.outlineOffset = '-2px';
  }

  function restore(el) {
    el.style.outline = el.__rainSavedOutline || '';
    el.style.outlineOffset = el.__rainSavedOffset || '';
    delete el.__rainSavedOutline;
    delete el.__rainSavedOffset;
  }

  function onMouseOver(e) {
    var t = e.target;
    if (t === _hovered) return;
    if (_hovered && _hovered !== _selected) restore(_hovered);
    _hovered = t;
    if (_hovered !== _selected) applyHover(_hovered);
  }

  function onMouseOut(e) {
    var t = e.target;
    if (t !== _selected) restore(t);
    if (_hovered === t) _hovered = null;
  }

  function onClick(e) {
    e.preventDefault();
    e.stopPropagation();
    if (_selected && _selected !== e.target) {
      restore(_selected);
    }
    _selected = e.target;
    applySelected(_selected);
    window.parent.postMessage({ type: 'RAIN_ELEMENT_SELECTED', data: getInfo(_selected) }, '*');
  }

  function destroy() {
    document.removeEventListener('mouseover', onMouseOver);
    document.removeEventListener('mouseout', onMouseOut);
    document.removeEventListener('click', onClick, true);
    if (_selected) restore(_selected);
    if (_hovered && _hovered !== _selected) restore(_hovered);
    _hovered = null;
    _selected = null;
    window.__rainVisualEditor = false;
  }

  document.addEventListener('mouseover', onMouseOver);
  document.addEventListener('mouseout', onMouseOut);
  document.addEventListener('click', onClick, true);

  window.addEventListener('message', function (e) {
    if (e.data && e.data.type === 'RAIN_EXIT_EDIT') destroy();
  });
})();
`

/**
 * Composable that manages visual element-selection inside an iframe.
 *
 * Usage:
 *   const { isEditMode, selectedElement, toggleEditMode, exitEditMode,
 *           clearSelectedElement, onIframeLoad, buildPromptWithContext, cleanup }
 *     = useVisualEditor(iframeRef)
 *
 * - Call `onIframeLoad` from the iframe's @load event so the editor script
 *   is re-injected after every navigation / reload.
 * - Call `cleanup` in onUnmounted to remove the window message listener.
 */
export function useVisualEditor(iframeRef: Ref<HTMLIFrameElement | null>) {
  const isEditMode = ref(false)
  const selectedElement = ref<SelectedElementInfo | null>(null)

  // ── Script injection ──────────────────────────────────────────────────────

  const injectScript = () => {
    const doc = iframeRef.value?.contentDocument
    if (!doc) return
    try {
      if (doc.getElementById('__rain-visual-editor-script')) return
      const script = doc.createElement('script')
      script.id = '__rain-visual-editor-script'
      script.textContent = EDITOR_SCRIPT
      ;(doc.head ?? doc.body)?.appendChild(script)
    } catch (err) {
      console.warn('[useVisualEditor] Script injection failed:', err)
    }
  }

  const sendExitSignal = () => {
    try {
      iframeRef.value?.contentWindow?.postMessage({ type: 'RAIN_EXIT_EDIT' }, '*')
    } catch {
      // cross-origin or iframe not ready — ignore
    }
  }

  // ── Public API ────────────────────────────────────────────────────────────

  /** Toggle visual edit mode on/off */
  const toggleEditMode = () => {
    isEditMode.value = !isEditMode.value
    if (isEditMode.value) {
      injectScript()
    } else {
      sendExitSignal()
      selectedElement.value = null
    }
  }

  /** Programmatically exit edit mode and clear selection (call before sending a message) */
  const exitEditMode = () => {
    if (isEditMode.value) {
      isEditMode.value = false
      sendExitSignal()
    }
    selectedElement.value = null
  }

  /** Remove the currently selected element without exiting edit mode */
  const clearSelectedElement = () => {
    selectedElement.value = null
  }

  /**
   * Call this from the iframe's @load event.
   * Re-injects the editor script if edit mode is still active after a reload.
   */
  const onIframeLoad = () => {
    if (isEditMode.value) {
      injectScript()
    }
  }

  /**
   * Enriches the user message with context about the selected element.
   * Returns the original message unchanged when no element is selected.
   */
  const buildPromptWithContext = (userMessage: string): string => {
    const el = selectedElement.value
    if (!el) return userMessage

    const lines: string[] = ['我需要修改页面中的一个元素，元素信息如下：', `- 标签：<${el.tagName}>`]
    if (el.id) lines.push(`- ID：#${el.id}`)
    if (el.classes) lines.push(`- 类名：.${el.classes.trim().split(/\s+/).join(' ')}`)
    if (el.text) lines.push(`- 文本内容：${el.text}`)
    lines.push(`- XPath：${el.xpath}`, '', `我的需求：${userMessage}`)
    return lines.join('\n')
  }

  // ── Window message listener ───────────────────────────────────────────────

  const handleWindowMessage = (event: MessageEvent) => {
    if (event.data?.type === 'RAIN_ELEMENT_SELECTED') {
      selectedElement.value = event.data.data as SelectedElementInfo
    }
  }

  window.addEventListener('message', handleWindowMessage)

  /** Call in onUnmounted to remove the global message listener */
  const cleanup = () => {
    window.removeEventListener('message', handleWindowMessage)
    sendExitSignal()
  }

  return {
    isEditMode,
    selectedElement,
    toggleEditMode,
    exitEditMode,
    clearSelectedElement,
    onIframeLoad,
    buildPromptWithContext,
    cleanup,
  }
}
