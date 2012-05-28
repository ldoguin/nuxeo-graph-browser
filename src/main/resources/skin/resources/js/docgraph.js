//
//  main.js
//
//  A project template for using arbor.js
//

function drawRelations(baseUrl, docId){
    var sys = arbor.ParticleSystem(1000, 600, 0.5) // create the system with sensible repulsion/stiffness/friction
    sys.parameters({gravity:true}) // use center-gravity to make the graph settle nicely (ymmv)
    var handler = new GraphEventHandler("#viewport", sys, baseUrl);
    sys.renderer = Renderer("#viewport", handler) // our newly created renderer will have its .init() method called shortly by sys...
    handler.addFirstNode(docId);
}

  
function Renderer(canvas, handler){
	    var canvas = $(canvas).get(0)
	    var ctx = canvas.getContext("2d");
	    var gfx = arbor.Graphics(canvas)
	    var particleSystem

	    var that = {
	      init:function(system){
	        //
	        // the particle system will call the init function once, right before the
	        // first frame is to be drawn. it's a good place to set up the canvas and
	        // to pass the canvas size to the particle system
	        //
	        // save a reference to the particle system for use in the .redraw() loop
	        particleSystem = system

	        // inform the system of the screen dimensions so it can map coords for us.
	        // if the canvas is ever resized, screenSize should be called again with
	        // the new dimensions
	        particleSystem.screenSize(canvas.width, canvas.height) 
	        particleSystem.screenPadding(80) // leave an extra 80px of whitespace per side
	        
	        // set up some event handlers to allow for node-dragging
	        that.initMouseHandling()
	      },
	      
	      redraw:function(){
	        // 
	        // redraw will be called repeatedly during the run whenever the node positions
	        // change. the new positions for the nodes can be accessed by looking at the
	        // .p attribute of a given node. however the p.x & p.y values are in the coordinates
	        // of the particle system rather than the screen. you can either map them to
	        // the screen yourself, or use the convenience iterators .eachNode (and .eachEdge)
	        // which allow you to step through the actual node objects but also pass an
	        // x,y point in the screen's coordinate system
	        // 
	        ctx.fillStyle = "white"
	        ctx.fillRect(0,0, canvas.width, canvas.height)
	        
	        particleSystem.eachEdge(function(edge, pt1, pt2){
	          // edge: {source:Node, target:Node, length:#, data:{}}
	          // pt1:  {x:#, y:#}  source position in screen coords
	          // pt2:  {x:#, y:#}  target position in screen coords

	          // draw a line from pt1 to pt2
	          ctx.strokeStyle = "rgba(0,0,0, .333)"
	          ctx.lineWidth = 1
	          ctx.beginPath()
	          ctx.moveTo(pt1.x, pt1.y)
	          ctx.lineTo(pt2.x, pt2.y)
	          ctx.stroke()
	        })

	        particleSystem.eachNode(function(node, pt){
	          var w = node.data.size;
	          var fontSize = 14
	          if (node.data.type=='virtual') {
	        	  	             fontSize = 9
	             gfx.rect(pt.x-w/2, pt.y-8, w, 20, 4, {fill:node.data.color, alpha:node.data.alpha})
	          } else {
	             gfx.oval(pt.x-w/2, pt.y-w/2, w, w, {fill:node.data.color, alpha:node.data.alpha})
	          }
	          var label = node.data.label;
	          if (node.data.open==false) {
	             label = label + "+";
	          }
	          gfx.text(label, pt.x, pt.y+7, {color:"white", align:"center", font:"Arial", size: fontSize})
	        })    			
	      },

	      initMouseHandling:function(){
	        // no-nonsense drag and drop (thanks springy.js)
	        var dragged = null; 
	        // start listening
	        $(canvas).mousedown(handler.clicked);
	      },
	      
	    }
	    return that
};
