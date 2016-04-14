(function (requirejs) {  // Wrap RequireJS-config in function (standard JS-practice to avoid pollutin global namespace)
  'use strict';

  requirejs.config({     // config for RequireJS:
    shim: {
      'jsRoutes': {
        deps: [],
        exports: 'jsRoutes'  // tell RequireJS about jsRoutes (which is generated on-the-fly in main.scala.html
                             // by giving name of the var defining the jsRoutes
      }
    },
    paths: {  // configure path to jquery-dep
      'jquery': ['../lib/jquery/jquery']
    }
  });

  requirejs.onError = function (err) {  // error output to browser console:
    console.log(err);
  };

  require(['jquery'], function ($) { // require jquery:
// code from before requirejs-wrappage:
    $(document).ready(function () {
      $('#button').on('click', function () {
        //$('#text').text('Hello');   make an Ajax call to server instead:
        jsRoutes.controllers.Application.text().ajax({
          success: function (text) {
            $('#text').text(text);
          }, error: function () {
              alert("oops, something went wrong with the ajax-amsterdam call via jsRoutes.controllers.Application.text()");
          }
        });//ajax
      });//on-click
    });//doc-ready
  });//require-jq
})(requirejs);//execute requirejs-fn i.e. this script itself.

