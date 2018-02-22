$(document).ready(function() {
    var options = {
        target: 'body', //把服务器返回的内容放入id为output的元素中      
        beforeSubmit: showRequest, //提交前的回调函数
        // data: $.param($('#alipayment').serialize());  
        success: showResponse, //提交后的回调函数  
        url: 'order/wap_pay_for_aliPay.do', //默认是form的action， 如果申明，则会覆盖  
        type: 'post', //默认是form的method（get or post），如果申明，则会覆盖  
        dataType: 'json', //html(默认), xml, script, json...接受服务端返回的类型  
        //clearForm: true,          //成功提交后，清除所有表单元素的值  
        //resetForm: true,          //成功提交后，重置所有表单元素的值  
        timeout: 10000 //限制请求的时间，当请求大于3秒后，跳出请求  
    }

    function showRequest(formData, jqForm, options) {
        //formData: 数组对象，提交表单时，Form插件会以Ajax方式自动提交这些数据，格式如：[{name:user,value:val },{name:pwd,value:pwd}]  
        //jqForm:   jQuery对象，封装了表单的元素     
        //options:  options对象  

        var queryString = $.param(formData); //name=1&address=2  
        console.log(queryString);
        // var formElement = jqForm[0]; //将jqForm转换为DOM对象  
        // var address = formElement.address.value; //访问jqForm的DOM元素  
        return true; //只要不返回false，表单都会提交,在这里可以对表单元素进行验证  

    };

    function showResponse(result) {
        console.log('response data:' + result)

        if (result.status == 0) {
            $('body').html('');
            $('body').html(result.data).show();
        } else {
            alert(result.msg);
        }
    };

    $("#alipayment_form").ajaxForm(options);

    // $("#alipayment_submit").submit(funtion() {
    //     $(this).ajaxSubmit(options);
    //     return false; //阻止表单默认提交  
    // });

    $('#alipayment_submit').submit(function() {
        $.ajax({
                url: 'http://localhost:8080/epay-mall/user/login.do',
                type: 'POST',
                contentType: 'application/x-www-form-urlencoded',
                // dataType: 'default: Intelligent Guess (Other values: xml, json, script, or html)',
                data: {
                    'username': 'admin',
                    'password': 'admin'
                }
            })
            .done(function() {
                console.log("success");

                $.ajaxSubmit(options);
                return false;

            })
            .fail(function() {
                console.log("error");
                return false;
            });

    });

});