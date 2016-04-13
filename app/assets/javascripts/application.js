$(document).ready(function () {
  $('#button').on('click', function () {
    //$('#text').text('Hello');   make an Ajax call to server instead:
    jsRoutes.controllers.Application.text().ajax({
      success: function (text) {
        $('#text').text(text);
      }, error: function () {
          alert("oops, something went wrong with the ajax-amsterdam call via jsRoutes.controllers.Application.text()");
      }
    });
  });
});

