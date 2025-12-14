$(function(){
	var $userRegister = $("#userRegister");
	
	$userRegister.validate({
		rules:{
			name:{
				required:true
			}
		},
		message:{
			name:{
				required:'Name required'
			}
		}
	})
	
})