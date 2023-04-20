'use strict';

const Foo = { template: '<div>foo</div>' }
const Bar = { template: '<div>bar</div>' }

const router = new VueRouter({
    mode: 'history',
    routes: [
        { path: '/foo', component: Foo },
        { path: '/bar', component: Bar }
    ]
});

const app = new Vue({
    router
}).$mount('#app');
