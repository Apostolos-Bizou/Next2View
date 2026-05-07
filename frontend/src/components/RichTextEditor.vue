<template>
  <div class="rte-wrapper" :class="{ 'rte-compact': compact, 'rte-disabled': disabled }">
    <!-- Toolbar -->
    <div v-if="editor && !disabled" class="rte-toolbar">
      <!-- Group 1: Text formatting -->
      <button type="button" class="rte-btn" :class="{ active: editor.isActive('bold') }"
        @click="editor.chain().focus().toggleBold().run()" :title="tt('editor.bold')">
        <strong>B</strong>
      </button>
      <button type="button" class="rte-btn" :class="{ active: editor.isActive('italic') }"
        @click="editor.chain().focus().toggleItalic().run()" :title="tt('editor.italic')">
        <em>I</em>
      </button>
      <button type="button" class="rte-btn" :class="{ active: editor.isActive('underline') }"
        @click="editor.chain().focus().toggleUnderline().run()" :title="tt('editor.underline')">
        <u>U</u>
      </button>

      <span class="rte-sep"></span>

      <!-- Group 2: Headings -->
      <button type="button" class="rte-btn rte-btn-h" :class="{ active: editor.isActive('heading', { level: 1 }) }"
        @click="editor.chain().focus().toggleHeading({ level: 1 }).run()" :title="tt('editor.heading1')">
        H<sub>1</sub>
      </button>
      <button type="button" class="rte-btn rte-btn-h" :class="{ active: editor.isActive('heading', { level: 2 }) }"
        @click="editor.chain().focus().toggleHeading({ level: 2 }).run()" :title="tt('editor.heading2')">
        H<sub>2</sub>
      </button>

      <span class="rte-sep"></span>

      <!-- Group 3: Lists -->
      <button type="button" class="rte-btn" :class="{ active: editor.isActive('bulletList') }"
        @click="editor.chain().focus().toggleBulletList().run()" :title="tt('editor.bulletList')">
        <span class="rte-ico">&bull;&equiv;</span>
      </button>
      <button type="button" class="rte-btn" :class="{ active: editor.isActive('orderedList') }"
        @click="editor.chain().focus().toggleOrderedList().run()" :title="tt('editor.orderedList')">
        <span class="rte-ico">1&equiv;</span>
      </button>

      <span class="rte-sep"></span>

      <!-- Group 4: Color picker -->
      <div class="rte-color-wrap">
        <button type="button" class="rte-btn rte-btn-color" :title="tt('editor.color')"
          @click.stop="toggleColorPicker" :style="{ borderBottomColor: currentColor }">
          A
        </button>
        <div v-if="showColorPicker" class="rte-color-popup" @click.stop>
          <button v-for="c in colors" :key="c.value" type="button" class="rte-color-swatch"
            :style="{ background: c.value }" :title="c.label" @click="setColor(c.value)"></button>
          <button type="button" class="rte-color-clear" @click="clearColor" :title="tt('editor.colorClear')">&times;</button>
        </div>
      </div>

      <!-- Group 5: Link -->
      <button type="button" class="rte-btn" :class="{ active: editor.isActive('link') }"
        @click="setLink" :title="tt('editor.link')">
        <span class="rte-ico">&#128279;</span>
      </button>

      <span class="rte-sep"></span>

      <!-- Group 6: History + clear -->
      <button type="button" class="rte-btn" @click="editor.chain().focus().undo().run()"
        :disabled="!editor.can().undo()" :title="tt('editor.undo')">
        <span class="rte-ico">&#8630;</span>
      </button>
      <button type="button" class="rte-btn" @click="editor.chain().focus().redo().run()"
        :disabled="!editor.can().redo()" :title="tt('editor.redo')">
        <span class="rte-ico">&#8631;</span>
      </button>
      <button type="button" class="rte-btn" @click="clearFormatting" :title="tt('editor.clearFormatting')">
        <span class="rte-ico">&#9003;</span>
      </button>
    </div>

    <!-- Editor content -->
    <editor-content :editor="editor" class="rte-content" :class="{ 'rte-content-compact': compact }" />
  </div>
</template>

<script setup>
import { ref, watch, onBeforeUnmount, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Underline from '@tiptap/extension-underline'
import Link from '@tiptap/extension-link'
import TextStyle from '@tiptap/extension-text-style'
import { Color } from '@tiptap/extension-color'

const { t: tt } = useI18n()

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  compact: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  minHeight: { type: String, default: '120px' },
})

const emit = defineEmits(['update:modelValue'])

const showColorPicker = ref(false)
const currentColor = ref('#1e3a8a')

const colors = computed(() => [
  { value: '#0f172a', label: tt('editor.colorBlack') },
  { value: '#1e3a8a', label: tt('editor.colorNavy') },
  { value: '#2563eb', label: tt('editor.colorBlue') },
  { value: '#16a34a', label: tt('editor.colorGreen') },
  { value: '#d97706', label: tt('editor.colorAmber') },
  { value: '#dc2626', label: tt('editor.colorRed') },
  { value: '#7c3aed', label: tt('editor.colorPurple') },
  { value: '#64748b', label: tt('editor.colorGray') },
])

// Initialize Tiptap editor
const editor = useEditor({
  content: props.modelValue,
  editable: !props.disabled,
  extensions: [
    StarterKit.configure({
      heading: { levels: [1, 2] },
      bulletList: { keepMarks: true, keepAttributes: false },
      orderedList: { keepMarks: true, keepAttributes: false },
    }),
    Underline,
    TextStyle,
    Color,
    Link.configure({
      openOnClick: false,
      autolink: true,
      HTMLAttributes: { rel: 'noopener noreferrer nofollow', target: '_blank' },
    }),
  ],
  editorProps: {
    attributes: {
      class: 'rte-prose',
      'data-placeholder': props.placeholder,
    },
  },
  onUpdate: ({ editor }) => {
    const html = editor.getHTML()
    // Treat empty editor as empty string (not "<p></p>")
    const isEmpty = html === '<p></p>' || html === ''
    emit('update:modelValue', isEmpty ? '' : html)
  },
})

// Sync external model changes (e.g. when form is reset/loaded)
watch(() => props.modelValue, (newValue) => {
  if (!editor.value) return
  const currentHtml = editor.value.getHTML()
  const incoming = newValue || ''
  // Avoid infinite loop: only update if different
  if (incoming !== currentHtml && !(incoming === '' && currentHtml === '<p></p>')) {
    editor.value.commands.setContent(incoming, false)
  }
})

// Sync disabled state
watch(() => props.disabled, (val) => {
  if (editor.value) editor.value.setEditable(!val)
})

// Close color picker on outside click
function handleOutsideClick(e) {
  if (showColorPicker.value && !e.target.closest('.rte-color-wrap')) {
    showColorPicker.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
  if (editor.value) editor.value.destroy()
})

// Color picker
function toggleColorPicker() {
  showColorPicker.value = !showColorPicker.value
}
function setColor(color) {
  currentColor.value = color
  editor.value.chain().focus().setColor(color).run()
  showColorPicker.value = false
}
function clearColor() {
  editor.value.chain().focus().unsetColor().run()
  showColorPicker.value = false
}

// Link
function setLink() {
  const previousUrl = editor.value.getAttributes('link').href
  const url = window.prompt(tt('editor.linkPrompt'), previousUrl || 'https://')
  if (url === null) return // cancelled
  if (url === '') {
    editor.value.chain().focus().extendMarkRange('link').unsetLink().run()
    return
  }
  // Basic URL validation
  let safeUrl = url.trim()
  if (!/^https?:\/\//i.test(safeUrl) && !/^mailto:/i.test(safeUrl)) {
    safeUrl = 'https://' + safeUrl
  }
  editor.value.chain().focus().extendMarkRange('link').setLink({ href: safeUrl }).run()
}

// Clear formatting
function clearFormatting() {
  editor.value.chain().focus().clearNodes().unsetAllMarks().run()
}
</script>

<style scoped>
.rte-wrapper {
  border: 1px solid var(--border-bright, #d1d5db);
  border-radius: 6px;
  background: var(--surface2, #f8fafc);
  transition: border-color 0.15s, background 0.15s;
  font-family: 'Nunito', sans-serif;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.rte-wrapper:focus-within {
  border-color: var(--accent, #2563eb);
  background: var(--surface, #ffffff);
}
.rte-wrapper.rte-disabled {
  opacity: 0.7;
  pointer-events: none;
}

/* Toolbar */
.rte-toolbar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 2px;
  padding: 6px 8px;
  background: var(--surface3, #f1f5f9);
  border-bottom: 1px solid var(--border, #e2e8f0);
  position: sticky;
  top: 0;
  z-index: 5;
}
.rte-compact .rte-toolbar {
  padding: 4px 6px;
}

/* Buttons */
.rte-btn {
  min-width: 28px;
  height: 28px;
  padding: 0 7px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 4px;
  color: var(--text-mid, #475569);
  font-family: 'Nunito', sans-serif;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: background 0.12s, color 0.12s, border-color 0.12s;
  user-select: none;
}
.rte-btn:hover:not(:disabled) {
  background: var(--surface, #ffffff);
  color: var(--text, #0f172a);
  border-color: var(--border-bright, #d1d5db);
}
.rte-btn.active {
  background: var(--accent-dim, #dbeafe);
  color: var(--accent, #2563eb);
  border-color: var(--accent, #2563eb);
}
.rte-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.rte-btn-h {
  font-size: 12px;
  letter-spacing: -0.5px;
}
.rte-btn-h sub {
  font-size: 9px;
  vertical-align: baseline;
  margin-left: 1px;
}
.rte-ico {
  font-size: 13px;
  line-height: 1;
  letter-spacing: -1px;
}

/* Separator */
.rte-sep {
  width: 1px;
  height: 18px;
  background: var(--border, #e2e8f0);
  margin: 0 4px;
}

/* Color picker */
.rte-color-wrap {
  position: relative;
  display: inline-block;
}
.rte-btn-color {
  border-bottom: 3px solid #1e3a8a;
  border-radius: 4px 4px 2px 2px;
  font-weight: 700;
}
.rte-color-popup {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
  padding: 6px;
  background: var(--surface, #ffffff);
  border: 1px solid var(--border-bright, #d1d5db);
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  z-index: 100;
}
.rte-color-swatch {
  width: 22px;
  height: 22px;
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 4px;
  cursor: pointer;
  padding: 0;
  transition: transform 0.1s;
}
.rte-color-swatch:hover {
  transform: scale(1.15);
}
.rte-color-clear {
  grid-column: span 4;
  height: 22px;
  background: var(--surface2, #f8fafc);
  border: 1px solid var(--border-bright, #d1d5db);
  border-radius: 4px;
  font-size: 11px;
  color: var(--text-mid, #475569);
  cursor: pointer;
  font-family: 'Nunito', sans-serif;
}
.rte-color-clear:hover {
  background: var(--surface3, #f1f5f9);
}

/* Editor content */
.rte-content {
  flex: 1;
  background: transparent;
}
:deep(.rte-prose) {
  outline: none;
  padding: 10px 12px;
  min-height: v-bind(minHeight);
  font-family: 'Nunito', sans-serif;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text, #0f172a);
}
.rte-content-compact :deep(.rte-prose) {
  min-height: 60px;
  padding: 8px 10px;
  font-size: 11.5px;
}

/* Placeholder when empty */
:deep(.rte-prose p.is-editor-empty:first-child::before) {
  content: attr(data-placeholder);
  color: var(--text-dim, #94a3b8);
  pointer-events: none;
  height: 0;
  float: left;
}

/* Prose typography */
:deep(.rte-prose p) {
  margin: 0 0 6px 0;
}
:deep(.rte-prose p:last-child) {
  margin-bottom: 0;
}
:deep(.rte-prose h1) {
  font-size: 18px;
  font-weight: 700;
  color: var(--text, #0f172a);
  margin: 8px 0 6px 0;
  line-height: 1.3;
}
:deep(.rte-prose h2) {
  font-size: 15px;
  font-weight: 700;
  color: var(--text, #0f172a);
  margin: 8px 0 4px 0;
  line-height: 1.3;
}
:deep(.rte-prose ul),
:deep(.rte-prose ol) {
  padding-left: 22px;
  margin: 4px 0 6px 0;
}
:deep(.rte-prose ul li),
:deep(.rte-prose ol li) {
  margin: 2px 0;
}
:deep(.rte-prose ul) {
  list-style-type: disc;
}
:deep(.rte-prose ol) {
  list-style-type: decimal;
}
:deep(.rte-prose strong) {
  font-weight: 700;
}
:deep(.rte-prose em) {
  font-style: italic;
}
:deep(.rte-prose u) {
  text-decoration: underline;
}
:deep(.rte-prose a) {
  color: var(--accent, #2563eb);
  text-decoration: underline;
  cursor: pointer;
}
:deep(.rte-prose a:hover) {
  color: #1d4ed8;
}

/* Mobile responsive */
@media (max-width: 600px) {
  .rte-toolbar {
    gap: 1px;
    padding: 4px 5px;
  }
  .rte-btn {
    min-width: 26px;
    height: 26px;
    padding: 0 5px;
    font-size: 12px;
  }
  .rte-sep {
    margin: 0 2px;
  }
}
</style>
