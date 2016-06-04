# TicDesign Website

[English](README.en.md)

为了更好的展示 TicDesign 的设计思路和使用方式，我们搭建了 [TicDesign 的网站][tic-design-site]。

## 修改网页代码

我们的网站使用 [Jekyll](http://jekyllrb.com/) 生成，托管在 [GitHub Pages](https://pages.github.com/) 上，通过 [GFM (GitHub Flavored Markdown)][gfm] 编写文档（设计和开发文档），并通过 [SASS][sass-lang] (Syntactically Awesome Style Sheets) 来获得非常灵活的文档样式开发。

网站源码的主要结构如下：

```
.
├── _data/
│   ├── links.yml   // 底部栏友链
│   └── tabs.yml    // 顶部栏链接
├── _design/    // 设计相关文件
│   ├── res/        // 设计资源（图片、小视屏等）
│   └── index.md    // 设计文档
├── _doc/       // 开发相关文件
│   └── index.md    // 开发文档
├── _includes/  // 页面的各个组件
├── _layouts/   // 页面的布局（目前就默认和文档两种布局）
├── assets/     // 网站资源文件
├── _config.yml // 网站配置
└── index.html  // 主页
```

如果你认为网站有什么问题，或者有更好的方案，欢迎提交 [Pull Request][ticdesign-pr]。

## 增加或修改文档

如果你认为我们的设计或开发文档有什么问题，或需要新增内容，欢迎修改设计文档 [_design/index.md](_design/index.md) 或开发文档 [_doc/index.md](_doc/index.md)

### 文档编写方式

我们使用 [GFM (GitHub Flavored Markdown)][gfm] 语法来编写文档，并通过内嵌 HTML 来支持丰富的资源展示。

GFM 语法请参考 [GitHub Guides][gfm]。

内嵌的 HTML，类似下面的形式编写：

``` markdown
这是一段 **Markdown** 语法的文本。

<div class="row">
<div class="col-half">
<img src="res/img1.png">
</div>
<div class="col-half">
<img src="res/img2.png">
</div>
</div>
```

我们为 TicDesign 的文档编写提供了较为灵活的布局方式。

首先，通过抽取 [Materialize][materialize] 的 Grid 系统，我们也提供了 12 格布局系统。通过指定 “行、列” 之间的关系和列宽占比，可以灵活的调整页面布局。 

> 12 格布局系统的详细说明参考 [Materialize - Grid](http://materializecss.com/grid.html)。

其次，我们为文档的布局做了特殊的调整，使得：

- 12格布局系统更好的适配文档样式要求。
- 文档有了定制化的更加简洁的样式属性。

定制化的样式属性如下：

1. `padding`: 通过给 `col` 指定 `padding`，可以使得你的列具有边距
2. `col-full`: 定义一个占满全屏的、有 padding 的、居中的列。
3. `col-half`: 定义一个占半屏的、有 padding 的、居中的列。
4. `col-third`: 定义一个占三分之一屏的、有 padding 的、居中的列。
5. `col-two-third`: 定义一个占三分之二屏的、有 padding 的、居中的列。

> 注：为了移动端更好的体验，我们定制的宽度属性，在手机小屏幕上都会占满整屏宽度。

> 详情可以查阅 [_sass/_post_grid_override.scss](_sass/_post_grid_override.scss)。

使用这些布局样式，你需要：

1. 将你内嵌的内容包装在 `<div class="row"></div>` 中。
2. 根据你的需要，通过 `<div class="col-*"></div>` 为你的内容分配合适宽度的大小。
3. 将你的内容（图片、视屏或文字）包装在 col 的 div 中。

你可以通过 [_design/index.md](_design/index.md) 来学习如何使用这些属性。

如果你使用的 Markdown 编辑器，支持导入自定义 css 来定义主题，你可以使用我们提供的 [TicDesign Doc Style][tic-doc-style]，使得你的 Markdown 编辑器的预览也可以支持我们定义的的布局样式。

> 也许，[TicDesign Doc Style][tic-doc-style] 可以单独成为一个开源项目，开放给大家在自己的文档或博客编写中使用。

最后，我们非常欢迎你对我们的网站或者文档提供[修改意见][ticdesign-issues]，或提交 [Pull Request][ticdesign-pr]。


[tic-design-site]: http://ticdesign.chumenwenwen.com/
[tic-doc-style]: http://ticdesign.chumenwenwen.com/assets/css/ticdesign-doc.css
[ticdesign-issues]: https://github.com/mobvoi/TicDesign/issues
[ticdesign-pr]: https://github.com/mobvoi/TicDesign/pulls
[sass-lang]: http://sass-lang.com/
[gfm]:https://guides.github.com/features/mastering-markdown/
[materialize]: http://materializecss.com/


