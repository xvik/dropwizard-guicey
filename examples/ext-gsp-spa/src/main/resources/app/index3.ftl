<#-- @ftlvariable name="" type="ru.vyarus.dropwizard.guice.examples.view.ComplexIndexView.Model" -->
<html lang="en">
<head>
    <link rel="stylesheet" href="style.css">
    <base href="${context.rootUrl}"/>
    <script src="https://unpkg.com/vue/dist/vue.js"></script>
    <script src="https://unpkg.com/vue-router/dist/vue-router.js"></script>
    <title>Sample SPA</title>
</head>
<body>

<div id="app">
    <h1>Sample routing</h1>
    <p>Dynamic value: ${sample}</p>
    <p>
        <router-link to="foo">Foo page</router-link> |
        <router-link to="bar">Bar page</router-link>
    </p>
    <router-view></router-view>
</div>

<script src="script.js"></script>
</body>
</html>
