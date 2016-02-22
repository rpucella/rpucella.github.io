
/*************************************************************
 * HACKATHON VIII
 * 
 * Salesforce assessment tool
 *
 *
 *  Jacob Willson 
 *  Theresa Pappafotopoulos 
 *  Riccardo Pucella
 *
 ************************************************************/


var GLOBAL = {
    data: null,
    view:"self_score",
    words:{"self_score":[],
	   "manager_score":[]},
    buyer: "None"
}
 

function run () {

    //loadExternalSVGs();

    setupData();

    setupTeams();

    printViewName();

    placeNamesInQuads();

    setupEvents();
    
    // report the data as a long tble...
    // dumpData();
}


function setupEvents () {

    //d3.select("#get-started-button")

    d3.select("#getstarted")
	.on("mouseenter",function() { d3.select("#getstarted-hover").style("display","inline"); });

    d3.select("#getstarted-hover")
	.style("cursor","pointer")
	.on("click",function() { slideTo(0,600); })
	.on("mouseleave",function() { d3.select("#getstarted-hover").style("display","none"); });

//    d3.select("#josephine-button")
//	.on("click",function() { slideTo(600,600); });

    d3.select("#home")
	.on("mouseenter",function() { d3.select("#home-hover").style("display","inline"); });

    d3.select("#home-hover")
	.style("cursor","pointer")
	.on("click",function() { disableActionPlan(); slideTo(0,600); })
	.on("mouseleave",function() { d3.select("#home-hover").style("display","none"); });

    d3.select("#previous")
	.on("mouseenter",function() { d3.select("#previous-hover").style("display","inline"); })

    d3.select("#previous-hover")
	.style("cursor","pointer")
	.on("click",function() { disableActionPlan(); slideTo(300,600); })
	.on("mouseleave",function() { d3.select("#previous-hover").style("display","none"); });


    d3.select("#home-action")
	.on("mouseenter",function() { d3.select("#home-action-hover").style("display","inline"); });

    d3.select("#home-action-hover")
	.style("cursor","pointer")
	.on("click",function() { disableActionPlan(); slideTo(0,600); })
	.on("mouseleave",function() { d3.select("#home-action-hover").style("display","none"); });

    d3.select("#previous-action")
	.on("mouseenter",function() { d3.select("#previous-action-hover").style("display","inline"); })

    d3.select("#previous-action-hover")
	.style("cursor","pointer")
	.on("click",function() { slideTo(900,600); })
	.on("mouseleave",function() { d3.select("#previous-action-hover").style("display","none"); });


    d3.select("#detailedview")
	.on("mouseenter",function() { d3.select("#detailedview-hover").style("display","inline"); });
    
    d3.select("#detailedview-hover")
	.style("cursor","pointer")
	.on("click",function() { enableActionPlan(); slideTo(900,600); })
	.on("mouseleave",function() { d3.select("#detailedview-hover").style("display","none"); });

    d3.select("#changeview")
	.on("click",switchView);

    d3.select("#actionplan")
	.on("mouseenter",function() { d3.select("#actionplan-hover").style("display","inline"); });
    
    d3.select("#actionplan-hover")
	.style("cursor","pointer")
	.on("click",function() { slideTo(1200,600); })
	.on("mouseleave",function() { d3.select("#actionplan-hover").style("display","none"); });

    for (var i=1; i<19; i++) {
	(function(closure_i){
	    d3.select("#qcontract-"+closure_i)
		.style("cursor","pointer")
		.on("click",function() { hideQuestionDetails(closure_i); });
	})(i);
    }

    enableQuestionClickEvents();

    d3.select("#selector-procurer")
	.style("cursor","pointer")
	.on("click",function() { setBuyerArchetype("Procurer");});

    d3.select("#selector-reactor")
	.style("cursor","pointer")
	.on("click",function() { setBuyerArchetype("Reactor");});

    d3.select("#selector-improver")
	.style("cursor","pointer")
	.on("click",function() { setBuyerArchetype("Improver");});

    d3.select("#selector-transformer")
	.style("cursor","pointer")
	.on("click",function() { setBuyerArchetype("Transformer");});
}


function setBuyerArchetype (arch) {
    GLOBAL.buyer = arch;

    var highlight = {Procurer: "#buyer-procurer",
		     Reactor: "#buyer-reactor",
		     Improver: "#buyer-improver",
		     Transformer: "#buyer-transformer"};

    d3.selectAll("#buyer-procurer,#buyer-reactor,#buyer-improver,#buyer-transformer,#buyer-procurer-mark,#buyer-reactor-mark,#buyer-improver-mark,#buyer-transformer-mark")
	.style("display","none");

    d3.selectAll(highlight[arch]+","+highlight[arch]+"-mark")
	.style("display","inline");

    
}


function enableActionPlan () {
    d3.selectAll("#detailedview,#detailedview-hover")
	.style("display","none");

    d3.select("#actionplan")
	.style("display","inline");

    d3.select("#actionplan-hover")
	.style("display","none");

}

function disableActionPlan () {
    d3.selectAll("#actionplan,#actionplan-hover")
	.style("display","none");

    d3.select("#detailedview")
	.style("display","inline");
}    

function enableQuestionClickEvents () {

    for (var i = 1; i < 19; i++) {
	(function(closure_i){ 
	    d3.select("#question-"+closure_i)
		.style("cursor","pointer")
		.on("click",function() { showQuestionDetails(closure_i); });
	})(i);
    }
}


function disableQuestionClickEvents () {

    for (var i = 1; i < 19; i++) {
	(function(closure_i){ 
	    d3.select("#question-"+closure_i)
		.style("cursor",null)
		.on("click",null);
	})(i);
    }
}
    

// FIXME - reset questions when moving out of the question details pane!


function showQuestionDetails (num) { 

    disableQuestionClickEvents();

    d3.select("#qexpand-"+num)
	.attr("transform","translate(0,300)")
	.style("display","inline")
	.transition()
	.duration(300)
	.attr("transform","translate(0,0)");

    var min_shown = Math.max(1,num-3);

    for (var i=1; i<min_shown; i++) {
	d3.select("#question-"+i)
	    .style("display","none");
    }

    d3.selectAll("#questions text.self")
	.style("display",function(d,i) { return (i+1 < min_shown) ? "none" : "inline"});
    d3.selectAll("#questions text.manager")
	.style("display",function(d,i) { return (i+1 < min_shown) ? "none" : "inline"});

    for (var i=min_shown; i<19; i++) {
	if (i !== num) {
	    d3.select("#question-"+i)
		.style("opacity","0.3");
	} else {
	    d3.select("#question-"+i)
		.style("font-weight","bold");
	}
    }
	    
    var delta = 88 / 3;
    var qs = d3.select("#questions")
	.transition()
	.duration(300)
	.attr("transform","translate(0,"+(-delta*(min_shown-1))+")");
}


function hideQuestionDetails (num) { 
    var id = "#qexpand-"+num;

    d3.select(id)
	.attr("transform","translate(0,0)")
	.transition()
	.duration(300)
	.attr("transform","translate(0,300)");

    d3.select("#questions")
	.transition()
	.duration(300)
	.attr("transform","translate(0,0)");

    setTimeout(function() { 
	d3.select(id).style("display","none"); 
	for (var i=0; i<19; i++) {
	    d3.select("#question-"+i)
		.style("display","inline")
		.style("opacity","1")
		.style("font-weight","normal");
	}

	d3.selectAll("#questions text.self")
	    .style("display","inline");
	d3.selectAll("#questions text.manager")
	    .style("display","inline");

	enableQuestionClickEvents();

    },300);

}

function slideTo (x,y) {
    console.log("Sliding to x =",x," y =",y);
    var vb = ""+x+" "+y+" 900 600";
    d3.select("#main-svg").transition().duration(300).attr("viewBox",vb);
    d3.select("#clip-rect").transition().duration(300).attr("x",x).attr("y",y);
}



function setupTeams () {
    var teams = GLOBAL.data.reduce(function(acc,current) { 
	if (acc.some(function(x) { return x===current.team; })) {
	    return acc;
	} else {
	    return acc.concat([current.team]); 
	}
    },[]);
    console.log("teams = ",teams);
    GLOBAL.teams = teams;

    d3.select("#main-g")
	.selectAll("text.filters")
	.data(teams)
	.enter()
	.append("text")
	.classed("filters selected",true)
	.attr("x",20)
	.attr("y",function(d,i) { return 1130 - (i*20); })
	.attr("dy","0.35em")
	.text(function(d) { return d;})
	.on("click", function(d) { var e = d3.select(this);
				   if (e.classed("selected")) {
				       e.classed("selected",false);
				       e.classed("unselected",true);
				       d3.selectAll("text.names-in-quads[data-team=\""+d+"\"]")
					   .property("selected","false")
					   .style("fill","#dddddd");
				   } else {
				       e.classed("selected",true);
				       e.classed("unselected",false);
				       d3.selectAll("text.names-in-quads[data-team=\""+d+"\"]")
					   .property("selected","true")
					   .style("fill",null);
				   }});
}


/*
function testCloud (f) {
    var names = GLOBAL.data.map(function(d) { return d.name; });
    console.log(names);
    var cloud = d3.layout.cloud()
	.size([300,300])
	.words(names.map(function(d) {return {text:d,size:15}; }))
	.padding(5)
	.rotate(0)
	.font("sans-serif")
	.fontSize(function(d) { return d.size; })
	// .text(function(d) { return d; })
	.on("end",draw)
	//.timeInterval(1000);

    cloud.start();
}
*/

function switchView () {
    var sw = {self_score:"manager_score",
	      manager_score:"self_score"};

    GLOBAL.view = sw[GLOBAL.view];
    printViewName();
    placeNamesInQuads();
}

function printViewName () {
    var txt = GLOBAL.view==="self_score" ? "Self assessment" : "Manager assessment";
    d3.select("#view-name").text(txt);
}

function placeNamesInQuads () {

    var byQuad = d3.nest()
	.key(function(d) { return classifyScore(d[GLOBAL.view]); })
	.map(GLOBAL.data);

    console.log("Data by quad = ",byQuad);

    layoutQuadrant(byQuad,[],Object.keys(byQuad))([]);
}


function layoutQuadrant (byQuad,accumulator,quads) {
    
    var quadMap = {"Order Taker":"quad1",
		   "Explainer":"quad2",
		   "Navigator":"quad3",
		   "Consultant":"quad4"};

    if (quads.length > 0) {

	var names = byQuad[quads[0]];
	
	return function(last) {

	    var cloud = d3.layout.cloud()
		.size([290,260])
		.words(names.map(function(d) {return {text:d.name,size:14,index:d.index,quad:quadMap[quads[0]],team:d.team}; }))
		.padding(5)
		.rotate(0)
		.font("sans-serif")
		.fontSize(function(d) { return d.size; })
		.spiral("rectangular")
		.on("end",layoutQuadrant(byQuad,accumulator.concat(last),quads.slice(1)));
	    //.timeInterval(1000);
	    
	    cloud.start();
		    
	}

    } else {

	return function (last) {
	    drawAll(accumulator.concat(last))
	}

    }
}

/*
    for (category in byQuad) {

	var names = byQuad[category]  //.map(function(d) { return d.name; })
	//console.log(names);

	var cloud = d3.layout.cloud()
	    .size([290,290])
	    .words(names.map(function(d) {return {text:d.name,size:18,index:d.index}; }))
	    .padding(5)
	    .rotate(0)
	    .font("sans-serif")
	    .fontSize(function(d) { return d.size; })
	    .spiral("rectangular")
	// .text(function(d) { return d; })
	    .on("end",draw(quadMap[category]))
	//.timeInterval(1000);

	cloud.start();
    }
}

*/


function showNameOverview (i) {

    console.log(GLOBAL.data[i]);
    var entry = GLOBAL.data[i];

    d3.select("#overview-salesperson-name")
	.text(entry.name)

    d3.select("#self-assessment-score")
	.text(entry.self_score)

    d3.select("#manager-assessment-score")
	.text(entry.manager_score)

    var xData = {"Order Taker": [[18,26],[920,982]],
		 "Explainer": [[27,45],[982,1045]],
		 "Navigator": [[46,64],[1045,1108]],
		 "Consultant": [[65,72],[1108,1170]]};

    var self_class = classifyScore(entry.self_score);
    var manager_class = classifyScore(entry.manager_score);
    
    x_self = d3.scale.linear()
	.domain(xData[self_class][0])
	.range([xData[self_class][1][0]-920,
		xData[self_class][1][1]-920]);

    x_manager = d3.scale.linear()
	.domain(xData[manager_class][0])
	.range([xData[manager_class][1][0]-920,
		xData[manager_class][1][1]-920]);

    d3.select("#self-assessment-dial_1_")
	.transition()
	.duration(300)
	.attr("transform","translate("+x_self(entry.self_score)+",0)");

    d3.select("#manager-assessment-dial_2_")
	.transition()
	.duration(300)
	.attr("transform","translate("+x_manager(entry.manager_score)+",0)");

    d3.selectAll("#self-class-Order-Taker,#self-class-Explainer,#self-class-Consultant,#self-class-Navigator,#manager-class-Order-Taker,#manager-class-Explainer,#manager-class-Consultant,#manager-class-Navigator")
	.attr("fill","#666666");

    d3.select("#self-class-"+(self_class.replace(" ","-")))
	.attr("fill","#9cdcf6")

    d3.select("#manager-class-"+(manager_class.replace(" ","-")))
	.attr("fill","#9cdcf6")

    slideTo(300,600);

    
    // ANSWERS TO THE QUESTIONS

    var color = function(d,i) { 
	if (Math.abs(entry.spread[i]) > 1) {
	    return "red";
	} else {
	    return "#878787";
	}
    }

    d3.select("#questions")
	.selectAll("text.self")
	.data(entry.self)
	.enter()
	.append("text")
	.classed("self",true)
	.style("font-family","sans-serif")
	.style("font-weight","bold")
	.style("font-size","14pt")
	// .style("fill",color)
	.attr("transform",function(d,i) { return "translate(400,0) "+d3.select("#question-"+(i+1)+" > text").attr("transform"); });

    d3.select("#questions")
	.selectAll("text.manager")
	.data(entry.manager)
	.enter()
	.append("text")
	.classed("manager",true)
	.style("font-family","sans-serif")
	.style("font-weight","bold")
	.style("font-size","14pt")
	// .style("fill",color)   // "#878787")
	.attr("transform",function(d,i) { return "translate(510,0) "+d3.select("#question-"+(i+1)+" > text").attr("transform"); });

    d3.selectAll("#questions > text.self")
	.style("fill",color)
	.text(function(d,i) { return d.toUpperCase(); });

    d3.selectAll("#questions > text.manager")
	.style("fill",color)
	.text(function(d,i) { return d.toUpperCase(); });

    
    // select an action plan

    d3.selectAll("#action-no-archetype,#action-aligned,#action-procurer,#action-reactor,#action-improver,#action-transformer")
	.style("display","none");

    
    var id;
    var archMap = {"None":"#action-no-archetype",
		   "Procurer":"#action-procurer",
		   "Reactor":"#action-reactor",
		   "Improver":"#action-improver",
		   "Transformer":"#action-transformer"};

    if (alignedArchetypes(manager_class,GLOBAL.buyer)) {
	id = "#action-aligned";
    } else {
	id = archMap[GLOBAL.buyer];
    }

    d3.select(id)
	.style("display","inline");

    return;


    // older version

    d3.selectAll("#details")
	.selectAll("text.questions")
	.data(QUESTIONS)
	.enter()
	.append("text")
	.classed("questions",true)
	.attr("x",1300)
	.attr("y",function(d,i) { return 640+(30*i); })
	.attr("dy","0.35em")
	.style("text-anchor","start")
	.style("font-size","20px")
	.text(function(d) { return d; })

    d3.selectAll("#details")
	.selectAll("text.self")
	.data(entry.self)
	.enter()
	.append("text")
	.classed("self",true)
	.attr("x",1500)
	.attr("y",function(d,i) { return 640+(30*i); })
	.attr("dy","0.35em")
	.style("text-anchor","start")
	.style("font-size","20px");

    d3.selectAll("#details")
	.selectAll("text.manager")
	.data(entry.manager)
	.enter()
	.append("text")
	.classed("manager",true)
	.attr("x",1600)
	.attr("y",function(d,i) { return 640+(30*i); })
	.attr("dy","0.35em")
	.style("text-anchor","start")
	.style("font-size","20px");

    d3.selectAll("#details > text")
	.text(function(d) { return d; });

}

function alignedArchetypes (seller, buyer) {

    var align = {"Order Taker":"Procurer",
		 "Explainer":"Reactor",
		 "Navigator":"Improver",
		 "Consultant":"Transformer"};

    return (buyer===align[seller]);
}
  

function drawAll (words) {
    console.log("Data to draw = ",words);

/*
    var quadOrigin = {quad1:{x:450,y:750},
		      quad2:{x:450,y:1050},
		      quad3:{x:750,y:1050},
		      quad4:{x:750,y:750}}[quad];
*/

    var quadTranslate = function(x,y) { 
	return {quad1:"translate("+(455+x)+","+(770+y)+")",
		quad2:"translate("+(455+x)+","+(1030+y)+")",
		quad3:"translate("+(745+x)+","+(1030+y)+")",
		quad4:"translate("+(745+x)+","+(770+y)+")"};
    }

    ///console.log("Quad origin",quadOrigin)

    var q = d3.select("#main-g")
	.selectAll("text.names-in-quads")
	.data(words,function(d) { return d.text; });

    q.enter().append("text")
	.classed("names-in-quads",true)
	.attr("data-team",function(d) { return d.team; })  // data- so that we can selectAll it
	.property("selected","true")   // by default, all teams are selected
	.attr("text-anchor", "middle")
	.style("font-family","sans-serif")
	.style("font-size",function(d) { return d.size+"px"; })
	.attr("dy",function(d) { return (d.size/2)+"px";})
	.style("cursor","pointer")
	//.attr("transform","translate(150,150)")
	.attr("transform",function(d) { return quadTranslate(0,0)[d.quad]; })
	.text(function(d) { return d.text; })
	.on("click",function(d) { if (this.selected==="true") { showNameOverview(d.index); }});

    q.exit().remove();

    d3.select("#main-g").selectAll("text.names-in-quads")
            .transition()
	    .attr("transform", function(d) {
		// return "translate(" + [d.x+150, d.y+150] + ")";
		return quadTranslate(d.x,d.y)[d.quad];
	    });
    
}


function draw (quad) {

    return function(words) {

    console.log("Drawing cloud");
    console.log("words = ",words);

    var quadOrigin = {quad1:{x:1350,y:150},
		      quad2:{x:1350,y:450},
		      quad3:{x:1650,y:450},
		      quad4:{x:1650,y:150}}[quad];

    console.log("Quad origin",quadOrigin)

    var q = d3.select("#"+quad)
	.selectAll("text.names")
	.data(words,function(d) { return d.text; });

    q.enter().append("text")
	.classed("names",true)
	.attr("text-anchor", "middle")
	.style("font-family","sans-serif")
	.style("font-size",function(d) { return d.size+"px"; })
	.attr("dy",function(d) { return (d.size/2)+"px";})
	.style("cursor","pointer")
	.attr("transform","translate(150,150)")
	.text(function(d) { return d.text; })
	.on("click",function(d) { console.log("clicked on ",d.text); console.log("index=",d.index); showNameOverview(d.index); });

    q.exit().remove();

    d3.select("#"+quad).selectAll("text.names")
            .transition()
	    .attr("transform", function(d) {
            return "translate(" + [d.x+150, d.y+150] + ")";
	})

    }
}


function setupData () {

    var data = readData();

    var d = [];
    var e = Object.keys(data);
    for (var i=0; i<e.length; i++) {
	entry = data[e[i]];
	entry.index = i;
	d.push(entry);
    }
    GLOBAL.data = d;

    console.log("Data = ",d);

    var m = d3.nest()
	.key(function(d) { return d[0]; })
	.key(function(d) { return d[1]; })
	.map(TRANSLATION_MAP);

    GLOBAL.translation = m;

    computeScores(m);
}

function computeScores (code) {

    var results = GLOBAL.data;

    var id;
    for (var i=0; i<results.length; i++) {
	///console.log(i);
	///console.log(results[i]);
	results[i].self_score = score(results[i].self,code);
	results[i].manager_score = score(results[i].manager,code);
	results[i].spread = computeSpread(results[i].self,results[i].manager,code);
    }
}

function computeSpread (self,manager,code) {
    var result = [];
    for (var i=0; i<self.length; i++) {
	result.push(answerValue(i+1,self[i])-answerValue(i+1,manager[i]));
    }
    return result;
}

function answerValue (question, answer) {
    var code = GLOBAL.translation;
    return code[question][answer.toUpperCase()][0][2];
}

function score (answers,code) {

    ///console.log(answers,code);
    var result = 0;
    for (var i=0; i<answers.length; i++) {
	result += answerValue(i+1,answers[i]); // code[i+1][answers[i].toUpperCase()][0][2];
    }
    return result;
}


function classifyScore (sc) {
    if (sc < 18) {
	return "?"
    } else if (sc < 27) {
	return "Order Taker";
    } else if (sc < 46) { 
	return "Explainer";
    } else if (sc < 65) { 
	return "Navigator";
    } else if (sc < 73) { 
	return "Consultant";
    } else {
	return "?";
    }
}


function dumpData () {
    
    var table = d3.select("body")
	.append("table")
	.attr("id","example")

    var tr = table.append("tr")
    tr.append("th")
	.text("Name");
    tr.append("th")
	.text("Self-assessment");
    tr.append("th")
	.text("Self-assessed score");
    tr.append("th")
	.text("archetype");
    tr.append("th")
	.text("Manager assessment");
    tr.append("th")
	.text("Manager score");
    tr.append("th")
	.text("archetype");
    
    tr = table.selectAll("tr.row")
	.data(GLOBAL.data)
	.enter()
	.append("tr")
	.classed("row",true);
    
    tr.append("td")
	.text(function(d) { return d.name; });

    tr.append("td")
	.text(function(d) { return d.self.toString(); });

    tr.append("td")
	.text(function(d) { return d.self_score.toString(); });

    tr.append("td")
	.text(function(d) { return classifyScore(d.self_score); });

    tr.append("td")
	.text(function(d) { return d.manager.toString(); });

    tr.append("td")
	.text(function(d) { return d.manager_score.toString(); });

    tr.append("td")
	.text(function(d) { return classifyScore(d.manager_score); });

}

function random(map,n) { 
    var row = [];
    var dict = ["a","b","c","d"];
    var rnd;
    for (var i=0; i<18; i++) {
	if (Math.random() < 0.2) { 
	    rnd = dict[Math.floor(Math.random() * 4)];
	} else {
	    rnd = map[i+1][n][0][1];
	}
	row.push(rnd.toLowerCase());
    }
    return row;
}


function readData () {

    var m = d3.nest()
	.key(function(d) { return d[0]; })
	.key(function(d) { return d[2]; })
	.map(TRANSLATION_MAP);

    var result = SAMPLE_DATA;

    var ids = Object.keys(result);
    for (var i=0; i<ids.length; i++) {
	result[ids[i]].team = (ids[i]==="REP#97" || ids[i]==="REP#98" || ids[i]==="REP#99") ? 
	                          "Imperial Team" : "Original Team";
    }

    for (var i=0; i<TEAM_W.length; i++) {
	result["w_"+i] = {name:TEAM_W[i],
			  self: random(m,Math.floor(Math.random()*4)+1),
			  manager: random(m,Math.floor(Math.random()*4)+1),
			  team: "W Team"};
    }

    return result;


    var ids = Object.keys(SAMPLE_DATA);

    for (var i=0; i<ids.length; i++) {
	result[ids[i]] = {name:SAMPLE_SELF_ASSESSMENT[ids[i]].name,
			  self:SAMPLE_SELF_ASSESSMENT[ids[i]].answers,
			  manager:SAMPLE_MANAGER_ASSESSMENT[ids[i]].answers};
    }

    return result;
}


// canonical structure:
//  id : {name:"...",
//        q1:{self: "a",
//            manager: "b"},
//        ...
//        extra: {}
//       }

// possibility: entry for self-assessments, entry for manager assessments

var SAMPLE_DATA = {
    "REP#1":{name:"Noah",
	     manager:["a","b","a","c","a","a","c","b","d","d","c","a","a","b","a","a","b","c"],
	     self:["d","c","a","d","c","c","a","c","b","c","d","e","a","a","e","b","b","c"]},
    "REP#2":{name:"Liam",
	     manager:["b","a","a","a","a","c","b","a","c","b","c","a","d","a","c","c","c","b"],
	     self:["b","b","d","b","b","a","b","c","d","c","c","b","a","a","b","d","a","d"]},
    "REP#3":{name:"Mason",
	     manager:["b","d","a","b","b","d","d","b","b","a","d","a","b","a","b","c","d","b"],
	     self:["a","b","b","b","c","c","a","a","c","d","c","b","a","d","c","c","d","c"]},
    "REP#4":{name:"Jacob",
	     manager:["b","d","d","b","d","a","a","d","d","b","c","e","d","a","e","a","d","a"],
	     self:["b","d","b","a","c","d","a","a","d","c","a","d","a","a","d","c","d","a"]},
    "REP#5":{name:"William",
	     manager:["b","c","d","b","d","d","b","c","b","d","b","c","b","b","c","d","d","c"],
	     self:["b","a","a","b","b","c","a","b","a","d","d","a","b","a","a","b","d","c"]},
    "REP#6":{name:"Ethan",
	     manager:["c","b","a","b","c","d","b","a","b","b","c","d","b","b","a","b","b","d"],
	     self:["c","c","d","b","a","b","c","d","c","b","a","e","b","b","d","b","c","a"]},
    "REP#7":{name:"Michael",
	     manager:["a","d","d","a","a","d","b","b","c","c","a","a","a","d","c","a","b","d"],
	     self:["c","c","b","c","d","d","a","a","c","a","b","e","c","a","a","c","a","c"]},
    "REP#8":{name:"Alexander",
	     manager:["c","d","b","c","d","a","b","b","a","d","c","b","b","c","e","b","d","d"],
	     self:["d","c","a","d","d","c","b","d","a","a","c","d","a","c","e","c","d","c"]},
    "REP#9":{name:"James",
	     manager:["a","c","a","d","d","a","d","c","a","c","d","d","d","d","c","c","d","c"],
	     self:["d","a","d","b","d","b","c","c","b","a","c","e","b","b","a","c","d","b"]},
    "REP#10":{name:"Daniel",
	      manager:["c","c","d","d","b","b","a","b","a","c","d","d","d","c","d","c","d","a"],
	      self:["d","a","d","c","b","c","d","c","a","a","c","a","d","a","e","c","c","c"]},
    "REP#11":{name:"Emma",
	      manager:["b","b","c","c","a","d","c","b","d","c","d","d","c","a","d","a","a","d"],
	      self:["c","c","a","d","a","b","d","b","a","a","d","b","b","a","e","b","c","c"]},
    "REP#12":{name:"Olivia",
	      manager:["a","d","a","b","d","a","d","c","d","b","a","b","b","a","e","d","d","a"],
	      self:["c","d","a","d","b","c","c","a","b","b","d","e","d","a","c","d","c","b"]},
    "REP#13":{name:"Sophia",
	      manager:["b","d","d","d","a","a","c","a","a","a","a","b","a","b","d","b","c","a"],
	      self:["b","c","d","a","d","b","c","b","a","c","b","e","b","d","e","c","c","c"]},
    "REP#14":{name:"Isabella",
	      manager:["a","a","b","a","b","a","d","b","a","c","d","a","c","c","e","b","c","a"],
	      self:["a","a","b","c","c","b","d","b","a","b","b","b","b","a","e","c","a","a"]},
    "REP#15":{name:"Ava",
	      manager:["d","b","a","a","c","b","d","c","a","b","c","d","b","a","d","b","a","d"],
	      self:["b","c","a","b","c","b","b","d","a","b","b","e","c","c","e","b","b","c"]},
    "REP#16":{name:"Mia",
	      manager:["c","a","c","a","b","d","c","a","b","d","b","e","b","c","c","a","d","d"],
	      self:["b","b","c","a","c","c","b","c","b","c","a","a","c","d","e","d","c","d"]},
    "REP#17":{name:"Emily",
	      manager:["c","a","b","b","b","a","a","a","c","c","b","a","a","d","e","a","b","c"],
	      self:["a","c","c","d","b","b","c","c","b","d","a","e","c","b","b","c","c","c"]},
    "REP#18":{name:"Abigail",
	      manager:["d","c","b","a","a","c","d","d","c","d","c","e","d","d","b","b","b","b"],
	      self:["c","c","a","b","c","d","c","b","a","a","b","e","b","d","a","d","d","d"]},
    "REP#19":{name:"Madison",
	      manager:["a","a","a","b","c","c","a","d","b","c","b","c","a","d","b","a","d","c"],
	      self:["c","d","c","c","b","c","c","b","a","c","a","c","c","a","b","a","a","c"]},
    "REP#20":{name:"Charlotte X",
	      manager:["c","a","a","d","b","a","c","d","d","d","a","c","a","c","d","b","b","a"],
	      self:["a","d","d","b","b","d","a","c","a","d","b","d","c","d","e","a","b","c"]},
    "REP#21":{name:"Charlotte Y",
	      manager:["b","b","b","a","d","a","c","a","c","a","a","c","a","c","c","c","c","b"],
	      self:["a","b","d","c","c","a","d","d","d","c","c","c","a","c","d","c","c","d"]},
    "REP#22":{name:"Kimberly",
	      manager:["b","b","b","d","d","c","a","c","c","b","d","d","d","c","d","b","d","a"],
	      self:["a","d","c","a","a","b","b","d","c","b","c","a","b","b","b","a","b","b"]},
    "REP#23":{name:"Lisa",
	      manager:["c","c","b","b","b","d","a","b","c","a","a","c","d","c","d","d","c","b"],
	      self:["b","d","c","a","a","b","c","a","c","c","a","e","d","a","e","b","b","c"]},
    "REP#24":{name:"Angela",
	      manager:["d","b","a","c","d","d","a","c","a","c","c","d","b","d","e","d","a","a"],
	      self:["c","a","d","b","a","d","b","d","d","a","b","e","c","a","a","c","b","d"]},
    "REP#25":{name:"Thomas",
	      manager:["d","c","d","a","a","b","b","b","c","b","b","c","c","d","b","b","a","d"],
	      self:["c","c","c","d","c","c","d","b","b","c","c","c","b","b","c","b","b","c"]},
    "REP#97":{name:"Gary",
	      manager:["b","a","d","b","b","d","b","a","d","b","c","a","b","a","c","a","a","d"],
	      self:["c","b","d","c","d","b","d","d","a","a","c","c","d","b","a","c","c","d"]},
    "REP#98":{name:"Napoleon",
	      manager:["b","a","c","b","a","d","b","c","d","b","d","a","a","a","b","a","a","d"],
	      self:["c","b","d","a","d","a","d","d","c","a","c","d","d","d","a","b","c","d"]},
    "REP#99":{name:"Josephine",
	      manager:["c","b","d","a","d","a","d","d","c","a","c","d","d","d","a","b","c","d"],
	      self:["b","a","c","b","a","d","b","c","d","b","d","a","a","a","b","a","a","d"]}
};

var TEAM_W = ["Walter","Wanda","Wei","Wendy","Wesley","Whitney","Wilhelmina","Willette","Willow","Wilma","Winifred","Winona"];



var SAMPLE_SELF_ASSESSMENT = {
    "REP#1":{name:"Noah",
	     manager:["d","c","a","d","c","c","a","c","b","c","d","e","a","a","e","b","b","c"]},
    "REP#2":{name:"Liam",
	     manager:["b","b","d","b","b","a","b","c","d","c","c","b","a","a","b","d","a","d"]},
    "REP#3":{name:"Mason",
	     manager:["a","b","b","b","c","c","a","a","c","d","c","b","a","d","c","c","d","c"]},
    "REP#4":{name:"Jacob",
	     manager:["b","d","b","a","c","d","a","a","d","c","a","d","a","a","d","c","d","a"]},
    "REP#5":{name:"William",
	     manager:["b","a","a","b","b","c","a","b","a","d","d","a","b","a","a","b","d","c"]},
    "REP#6":{name:"Ethan",
	     manager:["c","c","d","b","a","b","c","d","c","b","a","e","b","b","d","b","c","a"]},
    "REP#7":{name:"Michael",
	     manager:["c","c","b","c","d","d","a","a","c","a","b","e","c","a","a","c","a","c"]},
    "REP#8":{name:"Alexander",
	     manager:["d","c","a","d","d","c","b","d","a","a","c","d","a","c","e","c","d","c"]},
    "REP#9":{name:"James",
	     manager:["d","a","d","b","d","b","c","c","b","a","c","e","b","b","a","c","d","b"]},
    "REP#10":{name:"Daniel",
	      manager:["d","a","d","c","b","c","d","c","a","a","c","a","d","a","e","c","c","c"]},
    "REP#11":{name:"Emma",
	      manager:["c","c","a","d","a","b","d","b","a","a","d","b","b","a","e","b","c","c"]},
    "REP#12":{name:"Olivia",
	      manager:["c","d","a","d","b","c","c","a","b","b","d","e","d","a","c","d","c","b"]},
    "REP#13":{name:"Sophia",
	      manager:["b","c","d","a","d","b","c","b","a","c","b","e","b","d","e","c","c","c"]},
    "REP#14":{name:"Isabella",
	      manager:["a","a","b","c","c","b","d","b","a","b","b","b","b","a","e","c","a","a"]},
    "REP#15":{name:"Ava",
	      manager:["b","c","a","b","c","b","b","d","a","b","b","e","c","c","e","b","b","c"]},
    "REP#16":{name:"Mia",
	      manager:["b","b","c","a","c","c","b","c","b","c","a","a","c","d","e","d","c","d"]},
    "REP#17":{name:"Emily",
	      manager:["a","c","c","d","b","b","c","c","b","d","a","e","c","b","b","c","c","c"]},
    "REP#18":{name:"Abigail",
	      manager:["c","c","a","b","c","d","c","b","a","a","b","e","b","d","a","d","d","d"]},
    "REP#19":{name:"Madison",
	      manager:["c","d","c","c","b","c","c","b","a","c","a","c","c","a","b","a","a","c"]},
    "REP#20":{name:"Charlotte",
	      manager:["a","d","d","b","b","d","a","c","a","d","b","d","c","d","e","a","b","c"]},
    "REP#21":{name:"Charlotte",
	      manager:["a","b","d","c","c","a","d","d","d","c","c","c","a","c","d","c","c","d"]},
    "REP#22":{name:"Kimberly",
	      manager:["a","d","c","a","a","b","b","d","c","b","c","a","b","b","b","a","b","b"]},
    "REP#23":{name:"Lisa",
	      manager:["b","d","c","a","a","b","c","a","c","c","a","e","d","a","e","b","b","c"]},
    "REP#24":{name:"Angela",
	      manager:["c","a","d","b","a","d","b","d","d","a","b","e","c","a","a","c","b","d"]},
    "REP#25":{name:"Thomas",
	      manager:["c","c","c","d","c","c","d","b","b","c","c","c","b","b","c","b","b","c"]},
    "REP#98":{name:"Napoleon",
	      manager:["c","b","d","a","d","a","d","d","c","a","c","d","d","d","a","b","c","d"]},
    "REP#99":{name:"Josephine",
	      manager:["b","a","c","b","a","d","b","c","d","b","d","a","a","a","b","a","a","d"]}
};

var QUESTIONS = [
    "Question 1",
    "Question 2",
    "Question 3",
    "Question 4",
    "Question 5",
    "Question 6",
    "Question 7",
    "Question 8",
    "Question 9",
    "Question 10",
    "Question 11",
    "Question 12",
    "Question 13",
    "Question 14",
    "Question 15",
    "Question 16",
    "Question 17",
    "Question 18"
];

var TRANSLATION_MAP = [
    [1,"A",3],
    [1,"B",1],
    [1,"C",4],
    [1,"D",2],
    [2,"A",1],
    [2,"B",4],
    [2,"C",2],
    [2,"D",3],
    [3,"A",3],
    [3,"B",2],
    [3,"C",1],
    [3,"D",4],
    [4,"A",4],
    [4,"B",1],
    [4,"C",2],
    [4,"D",3],
    [5,"A",1],
    [5,"B",3],
    [5,"C",2],
    [5,"D",4],
    [6,"A",4],
    [6,"B",3],
    [6,"C",2],
    [6,"D",1],
    [7,"A",2],
    [7,"B",1],
    [7,"C",3],
    [7,"D",4],
    [8,"A",3],
    [8,"B",2],
    [8,"C",1],
    [8,"D",4],
    [9,"A",2],
    [9,"B",3],
    [9,"C",4],
    [9,"D",1],
    [10,"A",4],
    [10,"B",1],
    [10,"C",2],
    [10,"D",3],
    [11,"A",2],
    [11,"B",3],
    [11,"C",4],
    [11,"D",1],
    [12,"A",1],
    [12,"B",2],
    [12,"C",3],
    [12,"D",4],
    [12,"E",1],
    [13,"A",1],
    [13,"B",2],
    [13,"C",3],
    [13,"D",4],
    [14,"A",1],
    [14,"B",2],
    [14,"C",3],
    [14,"D",4],
    [15,"A",4],
    [15,"B",1],
    [15,"C",2],
    [15,"D",3],
    [15,"E",1],
  [16,"A",1],
  [16,"B",4],
  [16,"C",3],
  [16,"D",2],
  [17,"A",1],
  [17,"B",2],
  [17,"C",4],
  [17,"D",3],
  [18,"A",2],
  [18,"B",1],
  [18,"C",3],
  [18,"D",4]
];


function loadExternalSVGs () { 

    // AH! From:
    //   http://bl.ocks.org/enjalot/1503463

    defs = d3.select("#main").append("defs");
    d3.html("cover.svg", function(d) {
        console.log("loading cover.svg", d)
        //get a selection of the image so we can pull out the icon
        xml = d3.select(d) 
        console.log("xml", xml.select("#image"), xml.select("#image").node());
        icon = document.importNode(xml.select("#image").node(), true)
        console.log("image", icon)
        mask = defs.append("svg:g")
            .attr("id", "cover")
        mask.node().appendChild(icon);
	
	d3.select("#cover_image")
	    .append("use")
	    .attr("xlink:href","#cover")
	    //.attr("transform","scale(0.3)");
    });
}
