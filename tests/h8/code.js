
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
    view:"self_score"
}
 

function run () {

    setupData();

    printViewName();

    placeNamesInQuads();

    setupEvents();
    
    // report the data as a long tble...
    // dumpData();
}


function setupEvents () {

    d3.select("#get-started-button")
	.on("click",function() { slideTo(900); });

//    d3.select("#josephine-button")
//	.on("click",function() { slideTo(1200); });

    d3.select("#return-to-overview-button")
	.on("click",function() { slideTo(900); });

    d3.select("#josephine-assessment-button")
	.on("click",function() { slideTo(1800); });

    d3.select("#return-to-salesperson-button")
	.on("click",function() { slideTo(1200); });
    
    d3.select("#switch-view-button")
	.on("click",switchView);
}

function slideTo (x) {
    console.log("Sliding to x =",x);
    var vb = ""+x+" 0 900 600";
    d3.select("#main").transition().duration(500).attr("viewBox",vb);
    d3.select("#clip-rect").transition().duration(500).attr("x",x);
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
    d3.select("#view-name").text(GLOBAL.view);
}

function placeNamesInQuads () {

    var byQuad = d3.nest()
	.key(function(d) { return classifyScore(d[GLOBAL.view]); })
	.map(GLOBAL.data);

    var quadMap = {"Order taker":"quad1",
		   "Explainer":"quad2",
		   "Navigator":"quad3",
		   "Consultant":"quad4"};

    console.log("byQuad = ",byQuad);

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

function showNameOverview (i) {

    console.log(GLOBAL.data[i]);
    var entry = GLOBAL.data[i];

    d3.select("#overview-salesperson-name")
	.text(entry.name)

    d3.select("#overview-salesperson-self-class")
	.text(entry.self_score+" ("+(classifyScore(entry.self_score))+")");

    d3.select("#overview-salesperson-manager-class")
	.text(entry.manager_score+" ("+(classifyScore(entry.manager_score))+")");

    slideTo(1200);

    d3.selectAll("#details")
	.selectAll("text.questions")
	.data(QUESTIONS)
	.enter()
	.append("text")
	.classed("questions",true)
	.attr("x",2200)
	.attr("y",function(d,i) { return 40+(30*i); })
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
	.attr("x",2400)
	.attr("y",function(d,i) { return 40+(30*i); })
	.attr("dy","0.35em")
	.style("text-anchor","start")
	.style("font-size","20px");

    d3.selectAll("#details")
	.selectAll("text.manager")
	.data(entry.manager)
	.enter()
	.append("text")
	.classed("manager",true)
	.attr("x",2500)
	.attr("y",function(d,i) { return 40+(30*i); })
	.attr("dy","0.35em")
	.style("text-anchor","start")
	.style("font-size","20px");

    d3.selectAll("#details > text")
	.text(function(d) { return d; });

    
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

    ///console.log(m);

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
    }
}


function score (answers,code) {

    ///console.log(answers,code);
    var result = 0;
    for (var i=0; i<answers.length; i++) {
	result += code[i+1][answers[i].toUpperCase()][0][2];
    }
    return result;
}


function classifyScore (sc) {
    if (sc < 18) {
	return "?"
    } else if (sc < 27) {
	return "Order taker";
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


function readData () {

    result = {}

    var ids = Object.keys(SAMPLE_SELF_ASSESSMENT);

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

var SAMPLE_MANAGER_ASSESSMENT = {
  "REP#1":{name:"Noah",
	   answers:["a","b","a","c","a","a","c","b","d","d","c","a","a","b","a","a","b","c"]},
  "REP#2":{name:"Liam",
	   answers:["b","a","a","a","a","c","b","a","c","b","c","a","d","a","c","c","c","b"]},
  "REP#3":{name:"Mason",
	   answers:["b","d","a","b","b","d","d","b","b","a","d","a","b","a","b","c","d","b"]},
  "REP#4":{name:"Jacob",
	   answers:["b","d","d","b","d","a","a","d","d","b","c","e","d","a","e","a","d","a"]},
  "REP#5":{name:"William",
	   answers:["b","c","d","b","d","d","b","c","b","d","b","c","b","b","c","d","d","c"]},
  "REP#6":{name:"Ethan",
	   answers:["c","b","a","b","c","d","b","a","b","b","c","d","b","b","a","b","b","d"]},
  "REP#7":{name:"Michael",
	   answers:["a","d","d","a","a","d","b","b","c","c","a","a","a","d","c","a","b","d"]},
  "REP#8":{name:"Alexander",
	   answers:["c","d","b","c","d","a","b","b","a","d","c","b","b","c","e","b","d","d"]},
  "REP#9":{name:"James",
	   answers:["a","c","a","d","d","a","d","c","a","c","d","d","d","d","c","c","d","c"]},
  "REP#10":{name:"Daniel",
	    answers:["c","c","d","d","b","b","a","b","a","c","d","d","d","c","d","c","d","a"]},
  "REP#11":{name:"Emma",
	    answers:["b","b","c","c","a","d","c","b","d","c","d","d","c","a","d","a","a","d"]},
  "REP#12":{name:"Olivia",
	    answers:["a","d","a","b","d","a","d","c","d","b","a","b","b","a","e","d","d","a"]},
  "REP#13":{name:"Sophia",
	    answers:["b","d","d","d","a","a","c","a","a","a","a","b","a","b","d","b","c","a"]},
  "REP#14":{name:"Isabella",
	    answers:["a","a","b","a","b","a","d","b","a","c","d","a","c","c","e","b","c","a"]},
  "REP#15":{name:"Ava",
	    answers:["d","b","a","a","c","b","d","c","a","b","c","d","b","a","d","b","a","d"]},
  "REP#16":{name:"Mia",
	    answers:["c","a","c","a","b","d","c","a","b","d","b","e","b","c","c","a","d","d"]},
  "REP#17":{name:"Emily",
	    answers:["c","a","b","b","b","a","a","a","c","c","b","a","a","d","e","a","b","c"]},
  "REP#18":{name:"Abigail",
	    answers:["d","c","b","a","a","c","d","d","c","d","c","e","d","d","b","b","b","b"]},
  "REP#19":{name:"Madison",
	    answers:["a","a","a","b","c","c","a","d","b","c","b","c","a","d","b","a","d","c"]},
  "REP#20":{name:"Charlotte",
	    answers:["c","a","a","d","b","a","c","d","d","d","a","c","a","c","d","b","b","a"]},
  "REP#21":{name:"Charlotte",
	    answers:["b","b","b","a","d","a","c","a","c","a","a","c","a","c","c","c","c","b"]},
  "REP#22":{name:"Kimberly",
	    answers:["b","b","b","d","d","c","a","c","c","b","d","d","d","c","d","b","d","a"]},
  "REP#23":{name:"Lisa",
	    answers:["c","c","b","b","b","d","a","b","c","a","a","c","d","c","d","d","c","b"]},
  "REP#24":{name:"Angela",
	    answers:["d","b","a","c","d","d","a","c","a","c","c","d","b","d","e","d","a","a"]},
  "REP#25":{name:"Thomas",
	    answers:["d","c","d","a","a","b","b","b","c","b","b","c","c","d","b","b","a","d"]}
};

var SAMPLE_SELF_ASSESSMENT = {
  "REP#1":{name:"Noah",
	   answers:["d","c","a","d","c","c","a","c","b","c","d","e","a","a","e","b","b","c"]},
  "REP#2":{name:"Liam",
	   answers:["b","b","d","b","b","a","b","c","d","c","c","b","a","a","b","d","a","d"]},
  "REP#3":{name:"Mason",
	   answers:["a","b","b","b","c","c","a","a","c","d","c","b","a","d","c","c","d","c"]},
  "REP#4":{name:"Jacob",
	   answers:["b","d","b","a","c","d","a","a","d","c","a","d","a","a","d","c","d","a"]},
  "REP#5":{name:"William",
	   answers:["b","a","a","b","b","c","a","b","a","d","d","a","b","a","a","b","d","c"]},
  "REP#6":{name:"Ethan",
	   answers:["c","c","d","b","a","b","c","d","c","b","a","e","b","b","d","b","c","a"]},
  "REP#7":{name:"Michael",
	   answers:["c","c","b","c","d","d","a","a","c","a","b","e","c","a","a","c","a","c"]},
  "REP#8":{name:"Alexander",
	   answers:["d","c","a","d","d","c","b","d","a","a","c","d","a","c","e","c","d","c"]},
  "REP#9":{name:"James",
	   answers:["d","a","d","b","d","b","c","c","b","a","c","e","b","b","a","c","d","b"]},
  "REP#10":{name:"Daniel",
	    answers:["d","a","d","c","b","c","d","c","a","a","c","a","d","a","e","c","c","c"]},
  "REP#11":{name:"Emma",
	    answers:["c","c","a","d","a","b","d","b","a","a","d","b","b","a","e","b","c","c"]},
  "REP#12":{name:"Olivia",
	    answers:["c","d","a","d","b","c","c","a","b","b","d","e","d","a","c","d","c","b"]},
  "REP#13":{name:"Sophia",
	    answers:["b","c","d","a","d","b","c","b","a","c","b","e","b","d","e","c","c","c"]},
  "REP#14":{name:"Isabella",
	    answers:["a","a","b","c","c","b","d","b","a","b","b","b","b","a","e","c","a","a"]},
  "REP#15":{name:"Ava",
	    answers:["b","c","a","b","c","b","b","d","a","b","b","e","c","c","e","b","b","c"]},
  "REP#16":{name:"Mia",
	    answers:["b","b","c","a","c","c","b","c","b","c","a","a","c","d","e","d","c","d"]},
  "REP#17":{name:"Emily",
	    answers:["a","c","c","d","b","b","c","c","b","d","a","e","c","b","b","c","c","c"]},
  "REP#18":{name:"Abigail",
	    answers:["c","c","a","b","c","d","c","b","a","a","b","e","b","d","a","d","d","d"]},
  "REP#19":{name:"Madison",
	    answers:["c","d","c","c","b","c","c","b","a","c","a","c","c","a","b","a","a","c"]},
  "REP#20":{name:"Charlotte",
	    answers:["a","d","d","b","b","d","a","c","a","d","b","d","c","d","e","a","b","c"]},
  "REP#21":{name:"Charlotte",
	    answers:["a","b","d","c","c","a","d","d","d","c","c","c","a","c","d","c","c","d"]},
  "REP#22":{name:"Kimberly",
	    answers:["a","d","c","a","a","b","b","d","c","b","c","a","b","b","b","a","b","b"]},
  "REP#23":{name:"Lisa",
	    answers:["b","d","c","a","a","b","c","a","c","c","a","e","d","a","e","b","b","c"]},
  "REP#24":{name:"Angela",
	    answers:["c","a","d","b","a","d","b","d","d","a","b","e","c","a","a","c","b","d"]},
  "REP#25":{name:"Thomas",
	    answers:["c","c","c","d","c","c","d","b","b","c","c","c","b","b","c","b","b","c"]}
}


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
