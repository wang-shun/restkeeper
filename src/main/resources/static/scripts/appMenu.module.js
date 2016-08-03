/*
 *
 *  (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

/**
 * @author v.lugovsky
 * created on 16.12.2015
 */
(function () {
    'use strict';

    angular.module('BlurAdmin.pages.appMenu', [])
        .config(routeConfig);

    /** @ngInject */
    function routeConfig($stateProvider, $urlRouterProvider) {

        //根据数据库查 菜单
        $.ajax({
            url: "/application/listAppForMenu",
            success: function (data) {
                if (data.success) {

                    $.each(data.content, function (i, item) {
                        $stateProvider.state('menu_' + item.id, {
                            url: '/menu',
                            title: item.name,
                            template: '<ui-view></ui-view>',
                            abstract: true,
                            sidebarMeta: {
                                icon: 'ion-grid',
                                order: item.id
                            }
                        });

                        $.each(item.functions, function (g, func) {
                            $stateProvider.state('menu_' + item.id + '.func_' + func.id, {
                                title: func.name,
                                url: '/func_' + func.id,
                                templateUrl: 'func-submit.html',
                                //abstract: true,
                                controller: "funcSubmitCtrl",
                                param: func.id,
                                sidebarMeta: {
                                    order: func.id
                                }
                            });
                            if(g == 0){
                                $urlRouterProvider.when('/menu_'+item.id,'/func_'+func.id);
                            }
                        });
                    });
                }
            }
        });
    }

})();
