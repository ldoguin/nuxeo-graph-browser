
  <canvas id="viewport" width="1000px" height="600px"></canvas>
  <div id="legend" style="float:right">
    <p style="color:red">Subject</p>
    <p style="color:grey">Predicate</p>
    <p style="color:blue">Object</p>
  </div>
  <script src="${skinPath}/js/lib/jquery-1.6.1.min.js"></script>

  <!-- run from the original source files: -->
  <script src="${skinPath}/js/lib/etc.js"></script>
  <script src="${skinPath}/js/lib/kernel.js"></script>
  <script src="${skinPath}/js/lib/graphics/colors.js"></script>
  <script src="${skinPath}/js/lib/graphics/primitives.js"></script>
  <script src="${skinPath}/js/lib/graphics/graphics.js"></script>
  <script src="${skinPath}/js/lib/tween/easing.js"></script>
  <script src="${skinPath}/js/lib/tween/tween.js"></script>
  <script src="${skinPath}/js/lib/physics/atoms.js"></script>
  <script src="${skinPath}/js/lib/physics/barnes-hut.js"></script>
  <script src="${skinPath}/js/lib/physics/physics.js"></script>
  <script src="${skinPath}/js/lib/physics/system.js"></script>
  <script src="${skinPath}/js/lib/dev.js"></script>

  <script src="${skinPath}/js/graphEventHandler.js"></script>
  <script src="${skinPath}/js/docgraph.js"></script>

  <script type="text/javascript">


  $(document).ready(function(){
   drawRelations('${This.path}', '${id}');
   var browserIFrame = $('#relationBrowserFrame', parent.document.body);
   browserIFrame.height($(document.body).height() + 30);
  })
  </script>