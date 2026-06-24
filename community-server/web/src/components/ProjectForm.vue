<template>
  <div class="project-form">
    <!-- 头部 -->
    <div class="form-header">
      <h3 class="form-title">个人项目</h3>
      <div class="form-actions">
        <el-button type="text" size="small" class="refresh-btn">
          <Refresh class="icon" />
        </el-button>
        <el-button type="text" size="small" class="delete-btn" @click="handleDelete">
          <Trash2 class="icon" />
        </el-button>
      </div>
    </div>

    <!-- 项目名称和角色 -->
    <div class="form-row">
      <el-form-item label="项目名称" class="form-item">
        <el-input v-model="form.projectName" placeholder="请输入项目名称" class="input-field" />
      </el-form-item>
      <el-form-item label="项目角色" class="form-item">
        <el-input v-model="form.role" placeholder="负责内容" class="input-field" />
      </el-form-item>
    </div>

    <!-- 项目链接 -->
    <div class="section">
      <h4 class="section-title">项目链接</h4>
      <div v-for="(link, index) in form.links" :key="index" class="link-row">
        <el-form-item class="link-item">
          <el-input 
            v-model="link.url" 
            placeholder="项目链接" 
            class="link-input"
          />
        </el-form-item>
        <el-form-item class="link-item">
          <el-input 
            v-model="link.text" 
            placeholder="显示文字" 
            class="link-input"
          />
        </el-form-item>
        <el-button 
          type="text" 
          size="small" 
          class="remove-link-btn"
          @click="removeLink(index)"
          v-if="form.links.length > 1"
        >
          <Minus class="icon" />
        </el-button>
        <el-button 
          type="text" 
          size="small" 
          class="add-link-btn"
          @click="addLink"
          v-if="index === form.links.length - 1"
        >
          <Plus class="icon" />
        </el-button>
      </div>
      <p class="link-hint">显示文字留空时，将自动显示域名或完整链接。链接仅支持 http:// 或 https:// 链接。</p>
    </div>

    <!-- 项目时间 -->
    <div class="section">
      <h4 class="section-title">项目时间</h4>
      <div class="time-row">
        <el-form-item class="time-item">
          <el-date-picker
            v-model="form.startDate"
            type="date"
            placeholder="开始时间"
            format="yyyy/MM/dd"
            value-format="yyyy/MM/dd"
            class="date-picker"
          />
        </el-form-item>
        <span class="time-separator">-</span>
        <el-form-item class="time-item">
          <el-date-picker
            v-model="form.endDate"
            type="date"
            placeholder="结束时间"
            format="yyyy/MM/dd"
            value-format="yyyy/MM/dd"
            :disabled="form.isCurrent"
            class="date-picker"
          />
        </el-form-item>
        <el-switch
          v-model="form.isCurrent"
          active-text="至今"
          class="current-switch"
        />
      </div>
    </div>

    <!-- 项目描述 -->
    <div class="section">
      <h4 class="section-title">项目描述</h4>
      <div class="editor-toolbar">
        <el-button type="text" size="small" @click="formatText('bold')">
          <Bold class="toolbar-icon" />
        </el-button>
        <el-button type="text" size="small" @click="formatText('italic')">
          <Italic class="toolbar-icon" />
        </el-button>
        <el-button type="text" size="small" @click="formatText('underline')">
          <Underline class="toolbar-icon" />
        </el-button>
        <el-button type="text" size="small" @click="formatText('strikethrough')">
          <Strikethrough class="toolbar-icon" />
        </el-button>
        <el-divider direction="vertical" />
        <el-button type="text" size="small" @click="formatText('undo')">
          <Undo class="toolbar-icon" />
        </el-button>
        <el-button type="text" size="small" @click="formatText('redo')">
          <Redo class="toolbar-icon" />
        </el-button>
        <el-divider direction="vertical" />
        <el-button type="text" size="small" @click="formatText('alignLeft')">
          <AlignLeft class="toolbar-icon" />
        </el-button>
        <el-button type="text" size="small" @click="formatText('alignCenter')">
          <AlignCenter class="toolbar-icon" />
        </el-button>
        <el-button type="text" size="small" @click="formatText('alignRight')">
          <AlignRight class="toolbar-icon" />
        </el-button>
        <el-divider direction="vertical" />
        <el-button type="text" size="small" @click="formatText('list')">
          <List class="toolbar-icon" />
        </el-button>
        <el-button type="text" size="small" @click="formatText('listOrdered')">
          <ListOrdered class="toolbar-icon" />
        </el-button>
        <el-divider direction="vertical" />
        <el-button type="text" size="small" @click="formatText('indentDecrease')">
          <IndentDecrease class="toolbar-icon" />
        </el-button>
        <el-button type="text" size="small" @click="formatText('indentIncrease')">
          <IndentIncrease class="toolbar-icon" />
        </el-button>
        <div class="toolbar-spacer"></div>
        <el-button type="primary" size="small" class="ai-button">
          <Sparkles class="toolbar-icon" />
          AI 润色
        </el-button>
      </div>
      <el-textarea
        v-model="form.description"
        placeholder="项目描述"
        class="description-textarea"
        :rows="6"
      />
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { 
  Refresh, Trash2, Plus, Minus,
  Bold, Italic, Underline, Strikethrough,
  Undo, Redo, AlignLeft, AlignCenter, AlignRight,
  List, ListOrdered, IndentDecrease, IndentIncrease,
  Sparkles
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 表单数据
const form = reactive({
  projectName: '',
  role: '',
  links: [
    { url: '', text: '' }
  ],
  startDate: '',
  endDate: '',
  isCurrent: false,
  description: ''
})

// 添加链接
const addLink = () => {
  form.links.push({ url: '', text: '' })
}

// 删除链接
const removeLink = (index) => {
  form.links.splice(index, 1)
}

// 格式化文本
const formatText = (action) => {
  ElMessage.info(`点击了 ${action} 按钮`)
}

// 删除项目
const handleDelete = () => {
  ElMessage.info('删除项目功能')
}
</script>

<style scoped>
.project-form {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 头部 */
.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.form-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.form-actions {
  display: flex;
  gap: 8px;
}

.icon {
  width: 18px;
  height: 18px;
}

.refresh-btn .icon {
  color: #999;
}

.delete-btn .icon {
  color: #f56c6c;
}

/* 项目名称和角色 */
.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.form-item {
  flex: 1;
  margin-bottom: 0;
}

.input-field {
  width: 100%;
}

/* 区块 */
.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  margin: 0 0 12px 0;
}

/* 链接区域 */
.link-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.link-item {
  flex: 1;
  margin-bottom: 0;
}

.link-input {
  width: 100%;
}

.remove-link-btn .icon,
.add-link-btn .icon {
  color: #666;
}

.link-hint {
  font-size: 12px;
  color: #999;
  margin: 8px 0 0 0;
  line-height: 1.5;
}

/* 时间区域 */
.time-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.time-item {
  margin-bottom: 0;
}

.date-picker {
  width: 160px;
}

.time-separator {
  color: #ccc;
}

.current-switch {
  margin-left: 8px;
}

/* 编辑器工具栏 */
.editor-toolbar {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #fafafa;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  margin-bottom: 8px;
  gap: 4px;
}

.toolbar-icon {
  width: 16px;
  height: 16px;
  color: #666;
}

.toolbar-spacer {
  flex: 1;
}

.ai-button {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
}

/* 描述文本框 */
.description-textarea {
  width: 100%;
  min-height: 120px;
}

.description-textarea textarea {
  resize: none;
}
</style>