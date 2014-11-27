var ACT = new Object();

$(function (){
  var controller = ACT.controller = $("body").data("controller");
  var action = ACT.action = $("body").data("action");

  if(jQuery.isFunction(ACT[controller])){
    ACT[controller]();
  }

  if (ACT[controller] != null){
    if(jQuery.isFunction(ACT[controller][action])){
      ACT[controller][action]();
    }
  }

});
