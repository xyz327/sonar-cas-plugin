function logoutMenuHandler() {
    var menuHandlerTimer = setInterval(function () {
        var elem1 = document.getElementById('global-navigation');
        if (!elem1) {
            return;
        }
        var elem2 = elem1.getElementsByClassName('js-user-authenticated')[0];
        if (!elem2) {
            return;
        }
        var elem3 = elem2.getElementsByTagName('a')[0];
        if (elem3) {
            elem3.addEventListener('click', logoutHandler);

            clearInterval(menuHandlerTimer);
        }
    }, 250);
}

function logoutHandler() {
    var logoutHandlerTimer = setInterval(function () {
        var elem1 = document.getElementById('global-navigation');
        if (! elem1) { return; }
        var elem2 = elem1.getElementsByClassName('js-user-authenticated')[0];
        if (! elem2) { return; }
        var elem3 = elem2.getElementsByClassName('popup')[0];
        if (! elem3) { return; }
        var elem4 = elem3.getElementsByTagName('a')[1];
        if (elem4) {
            elem4.addEventListener('click', function (event) {
                Ajax.post("/api/authentication/logout", null, function () {
                    window.location.href = 'CASLOGOUTURL';
                })
                event.stopImmediatePropagation();
                return false;
            });
            clearInterval(logoutHandlerTimer);
        }

    }, 100);
}
var Ajax = {
    get: function(url,fn){
        var xhr=new XMLHttpRequest();
        xhr.open('GET',url,false);
        xhr.onreadystatechange=function(){
            if(xhr.readyState==4){
                if(xhr.status==200 || xhr.status==304){
                    console.log(xhr.responseText);
                    fn.call(xhr.responseText);
                }
            }
        }
        xhr.send();
    },

    post: function(url,data,fn){
        var xhr=new XMLHttpRequest();
        xhr.open('POST',url,false);
        xhr.setRequestHeader('Content-Type','application/x-www-form-urlencoded');
        xhr.onreadystatechange=function(){
            if (xhr.readyState==4){
                if (xhr.status==200 || xhr.status==304){
                    // console.log(xhr.responseText);
                    fn.call(xhr.responseText);
                }
            }
        }

        xhr.send(data);
    }
}