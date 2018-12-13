var svgNS = "http://www.w3.org/2000/svg";

function merge(){
    var o = {};
    for (var i = arguments.length - 1; i >= 0; i --) {
        var s = arguments[i];
        for (var k in s) { o[k] = s[k]; };
    }
    return o;
}

var defaultOptions = {
    angle:Math.PI/4,
    innerRadius:0,
    radius:50,
    rotation:0,
    trigger:null,
    extendedTargetArea:false
};

function slicePath(angle,radius,innerRadius){
    var slice = document.createElementNS(svgNS,"path");
    var cos = Math.cos(angle);
    var sin = Math.sin(angle);
    var d = "m" + innerRadius + ",0 " +
        "L" + radius + ",0 " +
        "A" + radius + "," + radius + " 0 0,1 " + radius * cos + "," + radius * sin + " " +
        "L" + innerRadius * cos + "," + innerRadius * sin + " " +
        "A" + innerRadius + "," + innerRadius + " 0 0,0 " + innerRadius + ",0 " +
        "z";
    slice.setAttribute("d",d);
    return slice;
}

//creates a SVG <path> element from an options map
//the map contains the following keys:
//angle: the angle(width) of the slice in radians
//innerRadius: the inner radius
//radius: the (outer) radius
//rotation: the rotation of the slice in radians
//trigger: function to call when slice is clicked
//--
//All other keys are added as attributes to the <path> element in the form key="value"
function createSlice(options){
    if (options.extendedTargetArea) {
        var screenDiagonal = Math.sqrt(window.innerWidth*window.innerWidth + window.innerHeight*window.innerHeight);
        var visibleSlice = slicePath(options.angle, options.radius, options.innerRadius);
        visibleSlice.setAttribute("transform","translate(" + window.innerWidth + "," + window.innerHeight + ")rotate(" + options.rotation*360/(2*Math.PI) + " 0 0)");
        var targetSlice =  slicePath(options.angle, screenDiagonal, options.innerRadius);
        targetSlice.setAttribute("transform","translate(" + window.innerWidth + "," + window.innerHeight + ")rotate(" + options.rotation*360/(2*Math.PI) + " 0 0)");
        targetSlice.setAttribute("style","fill:#00000000 !important");
        targetSlice.addEventListener("mouseup",options.trigger);
        var group = document.createElementNS(svgNS,"g");
        group.appendChild(visibleSlice);
        group.appendChild(targetSlice);
        delete(options.angle);
        delete(options.radius);
        delete(options.innerRadius);
        delete(options.rotation);
        delete(options.trigger);
        delete(options.extendedTargetArea);
        for(key in options){
            group.setAttribute(key,options[key]);
        }
        return group;
    } else {
        var slice = slicePath(options.angle, options.radius, options.innerRadius);
        slice.setAttribute("transform","translate(" + options.radius + "," + options.radius + ")rotate(" + options.rotation*360/(2*Math.PI) + " 0 0)");
        slice.addEventListener("mouseup",options.trigger);
        delete(options.angle);
        delete(options.radius);
        delete(options.innerRadius);
        delete(options.rotation);
        delete(options.trigger);
        delete(options.extendedTargetArea);
        for(key in options){
            slice.setAttribute(key,options[key]);
        }
        return slice;
    }
};

//create a <svg> element containing a pie menu
//you can specify the contents of the pie menu in three different ways with the first argument:
//using a number will create a pie menu containing that many slices
//using an array of strings will create a pie menu containing as many slices as strings, with the strings as labels
//using an array of objects allows fine grained control over each slice, by specifying the parameters for the slice in the object via key:value pairs
//supported keys are
//angle: the angle(width) of the slice in radians
//innerRadius: the inner radius
//radius: the (outer) radius
//rotation: the rotation of the slice in radians
//trigger: function to call when slice is clicked
//--
//All other keys are added as attributes to the slice's <path> element in the form key="value"
//------
//pie optionally takes an object as a second argument
//the object has the same key:value pairs as mentioned above and is merged with each slice's options. It can be used to provide default options, that are the same across all slices.
function pie(arg,generalOptions){
    var t = typeof arg;
    if(t == "number"){
        return pieNum(arg,generalOptions);
    } else {
        if(typeof arg[0] == "string"){
            return pieLabels(arg,generalOptions);
        }
        if(typeof arg[0] == "object"){
            return pieDicts(arg,generalOptions);
        }
    }

    function pieNum(numSlices,generalOptions){
        var dicts = [];
        for (var i = 0; i < numSlices; i++){
            dicts[i] = {label:i};
        }
        return pieDicts(dicts,generalOptions);
    }

    function pieLabels(labels,generalOptions){
        var dicts = [];
        for(var i = 0; i < labels.length; i++){
            dicts[i] = {label:labels[i]};
        }
        return pieDicts(dicts);
    }

    function pieDicts(sliceOptions,generalOptions){
        var menu = document.createElementNS(svgNS,"svg");
        menu.setAttribute("xmlns",svgNS);
        menu.setAttribute("version","1.1");
        var n = sliceOptions.length;
        var angle = 2*Math.PI/n;
        for(var i = 0; i < n; i++){
            var options = sliceOptions[i];
            var slice = createSlice(merge(options,generalOptions,{angle:angle,rotation:i*angle},defaultOptions));
            menu.appendChild(slice);
        }
        var opts = merge(generalOptions,defaultOptions);
        var width = (options.extendedTargetArea ? window.innerWidth : opts.radius) * 2;
        var height = (options.extendedTargetArea ? window.innerHeight : opts.radius) * 2;
        menu.setAttribute("width",width);
        menu.setAttribute("height",height);
        return menu;
    }
}

function contextPie(slices,generalOptions){
    var menu = pie(slices,generalOptions);
    menu.style.position = "fixed";
    menu.style.display = "none";
    document.getElementsByTagName("body")[0].appendChild(menu);
    window.oncontextmenu = function (e){
        x = e.pageX;
        y = e.pageY;
        menu.style.left = x - (menu.width.baseVal.value/2);
        menu.style.top = y - (menu.height.baseVal.value/2);
        menu.style.display = "block";
        e.preventDefault();
    };
    window.onmouseup = function (e){
        menu.style.display = "none";
    };
}
