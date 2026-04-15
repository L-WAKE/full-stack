<template>
  <div class="page-shell">
    <section class="page-toolbar">
      <div class="page-title">
        <div>
          <span class="page-eyebrow">Model Settings</span>
          <h2>模型设置</h2>
          <p class="page-subtitle">统一整理 Chat、Embedding、Rerank 的配置边界，为后续供应商切换和密钥管理做准备。</p>
        </div>
      </div>
    </section>

    <section class="setting-grid">
      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Chat</span>
            <h2>对话模型</h2>
          </div>
        </div>
        <p class="page-subtitle">负责问答生成、SSE 流式输出和会话体验，是用户感知最直接的模型层。</p>
      </article>

      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Embedding</span>
            <h2>向量模型</h2>
          </div>
        </div>
        <p class="page-subtitle">负责文档切片向量化与检索召回，是知识入库和检索质量的基础能力。</p>
      </article>

      <article class="panel-card">
        <div class="page-title">
          <div>
            <span class="page-eyebrow">Rerank</span>
            <h2>重排能力</h2>
          </div>
        </div>
        <p class="page-subtitle">后续可接入重排阶段，提升片段排序质量和最终引用稳定性。</p>
      </article>
    </section>
  </div>
</template>

<style scoped>
.setting-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--space-4);
}

@media (max-width: 1100px) {
  .setting-grid {
    grid-template-columns: 1fr;
  }
}
</style>
