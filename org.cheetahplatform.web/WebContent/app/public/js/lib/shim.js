/**
 * Created by Uli-Lenovo on 14.10.2016.
 */
if (!String.prototype.startsWith) {
    String.prototype.startsWith = function (str){
        return this.lastIndexOf(str, 0) === 0;
    };
}