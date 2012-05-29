function GraphEventHandler(canvas, particleSystem, baseUrl) {

	this.canvas = $(canvas).get(0);
	this.particleSystem = particleSystem;
	this.baseUrl = baseUrl;
	var nodeStack = new Array();
	var me = this;
	var idx = 2;
	var nodeStackSizeBeforePrune = 3;
	GraphEventHandler.prototype.clicked = function(e) {
		var pos = $(canvas).offset();
		_mouseP = arbor.Point(e.pageX - pos.left, e.pageY - pos.top)
		dragged = particleSystem.nearest(_mouseP);

		if (dragged && dragged.node !== null) {
			// while we're dragging, don't let physics move the node
			dragged.node.fixed = true
		}

		if (dragged.node.data.type == 'virtual' && !dragged.node.data.open) {
			if (dragged.node.data.relation == 'hasTags') {
				me.addTags(dragged.node.data.uuid);
			} else if (dragged.node.data.relation == 'hasRelations') {
				me.addRelation(dragged.node.data.uuid);
			} else if (dragged.node.data.relation == 'parentOf') {
				me.addChildren(dragged.node.data.uuid);
			} else if (dragged.node.data.relation == 'childOf') {
				me.addParent(dragged.node.data.uuid);
			} else if (dragged.node.data.relation == 'tag') {
				me.addTagDocuments(dragged.node.data.label);
			}
			dragged.node.data.open = true;
		} else if (dragged.node.data.type == 'doc' && !dragged.node.data.open) {
			me.addFirstNode(dragged.node.data.uuid, true);
			dragged.node.data.open = true;
		}

		$(canvas).bind('mousemove', me.dragged)
		$(window).bind('mouseup', me.dropped)

		return false
	}

	GraphEventHandler.prototype.addFirstNode = function(uuid, prune) {
		$.getJSON(baseUrl + '/choices/' + uuid,
				function(data) {
					if (prune) {
						me.pruneGraph(uuid);
					}
					var nodeToAdd = new Array();
					var subjectItem;
					for ( var i = 0; i < data.length; i++) {
						var item = data[i];
						subjectLabel = 'doc:' + item.subject.uuid;
						predicateLabel = item.subject.uuid + ':'
								+ item.predicate.label;
						var node = particleSystem.addNode(subjectLabel,
								item.subject);
						nodeToAdd.push(node);
						node = particleSystem.addNode(predicateLabel,
								item.predicate);
						nodeToAdd.push(node);
						particleSystem.addEdge(subjectLabel, predicateLabel);
					}

					$("#title").text(item.subject.label);
					$("#description").text(item.subject.description);
					$("#uuid").text(item.subject.uuid);

					nodeStack.push(nodeToAdd);
				});
	}

	GraphEventHandler.prototype.addTags = function(uuid, prune) {
		$.getJSON(baseUrl + '/tag/list/' + uuid, function(data) {
			if (prune) {
				me.pruneGraph();
			}
			var nodeToAdd = new Array();
			for ( var i = 0; i < data.length; i++) {
				var item = data[i];
				subjectLabel = item.subject.uuid + ':hasTags';
				predicateLabel = 'isTag:' + item.predicate.uuid;
				var node = particleSystem.addNode(predicateLabel,
						item.predicate);
				nodeToAdd.push(node);
				particleSystem.addEdge(subjectLabel, predicateLabel);
			}
			nodeStack.push(nodeToAdd);
		});
	}

	GraphEventHandler.prototype.addTagDocuments = function(label) {
		$.getJSON(baseUrl + '/tag/documents/' + label, function(data) {
			if (prune) {
				me.pruneGraph();
			}
			var nodeToAdd = new Array();
			for ( var i = 0; i < data.length; i++) {
				var item = data[i];
				predicateLabel = 'isTag:' + label;
				objectLabel = 'doc:' + item.object.uuid;
				var node = particleSystem.addNode(objectLabel, item.object);
				nodeToAdd.push(node);
				particleSystem.addEdge(predicateLabel, objectLabel);
			}
			nodeStack.push(nodeToAdd);
		});
	}

	GraphEventHandler.prototype.addRelation = function(uuid, prune) {
		$.getJSON(baseUrl + '/relation/uuid/' + uuid, function(data) {
			if (prune) {
				me.pruneGraph();
			}
			var nodeToAdd = new Array();
			for ( var i = 0; i < data.length; i++) {
				var item = data[i];
				subjectLabel = item.subject.uuid + ':hasRelations';
				objectLabel = 'doc:' + item.object.uuid;
				predicateLabel = item.subject.uuid + ':' + item.predicate.label
						+ ':' + item.object.uuid;
				var node = particleSystem.addNode(objectLabel, item.object);
				nodeToAdd.push(node);
				node = particleSystem.addNode(predicateLabel, item.predicate);
				nodeToAdd.push(node);
				particleSystem.addEdge(subjectLabel, predicateLabel);
				particleSystem.addEdge(predicateLabel, objectLabel);
			}
			nodeStack.push(nodeToAdd);
		});
	}

	GraphEventHandler.prototype.addChildren = function(uuid, prune) {
		$.getJSON(baseUrl + '/children/' + uuid, function(data) {
			if (prune) {
				me.pruneGraph();
			}
			var nodeToAdd = new Array();
			for ( var i = 0; i < data.length; i++) {
				var item = data[i];
				predicateLabel = item.subject.uuid + ':parentOf';
				objectLabel = 'doc:' + item.object.uuid;
				var node = particleSystem.addNode(objectLabel, item.object);
				nodeToAdd.push(node);
				particleSystem.addEdge(predicateLabel, objectLabel);
			}
			nodeStack.push(nodeToAdd);
		});
	}

	GraphEventHandler.prototype.addParent = function(uuid, prune) {
		$.getJSON(baseUrl + '/parent/' + uuid, function(data) {
			if (prune) {
				me.pruneGraph();
			}
			var nodeToAdd = new Array();
			for ( var i = 0; i < data.length; i++) {
				var item = data[i];
				predicateLabel = item.subject.uuid + ':childOf';
				objectLabel = 'doc:' + item.object.uuid;
				var node = particleSystem.addNode(objectLabel, item.object);
				nodeToAdd.push(node);
				particleSystem.addEdge(predicateLabel, objectLabel);
			}
			nodeStack.push(nodeToAdd);
		});
	}

	GraphEventHandler.prototype.pruneGraph = function(uuid) {
		for ( var i = 0; i < nodeStack.length; i++) {
			var nodeToRemove = nodeStack[i];
			for ( var j = 0; j < nodeToRemove.length; j++) {
				if (nodeToRemove[j].data.uuid != uuid) {
					particleSystem.pruneNode(nodeToRemove[j]);
				}
			}
		}
	}

	GraphEventHandler.prototype.dragged = function(e) {
		var pos = $(canvas).offset();
		var s = arbor.Point(e.pageX - pos.left, e.pageY - pos.top)

		if (dragged && dragged.node !== null) {
			var p = particleSystem.fromScreen(s)
			dragged.node.p = p
		}

		return false
	}

	GraphEventHandler.prototype.dropped = function(e) {
		if (dragged === null || dragged.node === undefined)
			return

		if (dragged.node !== null)
			dragged.node.fixed = false
		dragged.node.tempMass = 1000
		dragged = null
		$(canvas).unbind('mousemove', me.dragged)
		$(window).unbind('mouseup', me.dropped)
		_mouseP = null
		return false
	}
};