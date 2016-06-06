# TicDesign

[![](https://api.bintray.com/packages/ticwear/maven/ticdesign/images/download.svg)](https://jcenter.bintray.com/com/ticwear/ticdesign/1.0.0/)

[English](README.md)

使用 TicDesign，你可以轻松让你的手表应用获得优雅的用户交互，并符合 Ticwear 的[设计规范][ticwear-design]。如果你的应用运行在 Ticwear 系统上，它还可以支持挠挠等新型交互方式。

<img src="art/settings.png" width="320">
<img src="art/delay-confirm-dialog.png" width="320">

访问 [TicDesign 官方网站][ticdesign-site]，深入了解 TicDeign 的设计思想、功能和使用方式。

# 如何使用

在需要使用的 module 中，添加对 TicDesign 的依赖：

``` gradle
dependencies {
    compile 'com.ticwear:ticdesign:1.0.0'
}
```

详细的使用帮助，参看 [TicDesign 开发文档][ticdesign-develop]。

# 遇到问题？

如有疑问或遇到 bug，请提交 [issues][ticdesign-issues]。

# 贡献

如果你希望为这个开源库贡献代码、文档或任何能帮助到其他开发者的东西。我们欢迎你提交 [Pull Request][ticdesign-pr]。

## 修改代码

请往 `dev` 分支提交代码，我们帮助你审查后，会合并你的代码。经过测试后，`dev` 分支的代码将会发布到 `master` 分支，并部署到代码中心。

如果你有新的控件需要增加到 TicDesign，请：

1. 把控件增加到 `ticwear.design.widget` 包中。
2. 将控件涉及到的资源，放入 `*_widget.xml` 中。
3. 为你的新控件增加文档（增加方式参看下面的修改文档）。

## 修改文档

TicDesign 的文档，以[网站形式][ticdesign-site]发布，托管在 [GitHub Pages][gh-pages] 上。如果你希望修改或增加文档，请修改 `gh-pages` 分支的 Markdown 格式文档。

详情请看 `gh-pages` 分支的 [README.md][gh-pages-readme]。

# 开源协议

``` txt
Copyright (c) 2016 Mobvoi Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[ticwear-design]: http://ticdesign.chumenwenwen.com/design/
[ticdesign-site]: http://ticdesign.chumenwenwen.com/
[ticdesign-develop]: http://ticdesign.chumenwenwen.com/doc/
[ticdesign-issues]: https://github.com/mobvoi/TicDesign/issues
[ticdesign-pr]: https://github.com/mobvoi/TicDesign/pulls
[gh-pages]: https://pages.github.com/
[gh-pages-readme]: https://github.com/mobvoi/ticdesign/blob/gh-pages/README.md

