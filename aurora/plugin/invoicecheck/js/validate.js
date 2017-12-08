/**
 * @Author: xuzhao
 * @Email: mailto:zhao.xu@hand-china.com
 * @Description: 数据校验js,文件地址：https://inv-veri.chinatax.gov.cn/js
 * @Time: 2017/10/17 15:23
 */
var flag = "",
    code = new Array('144031539110', '131001570151', '133011501118', '111001571071'),
    code10 = new Array('1440315391', '1310015701', '1330115011', '1110015710');

var alxd = function(a){
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 判断发票类型
     * @Time: 2017/10/17 15:32
     * @param a 发票代码
     * @Return: 发票类型号
     */
    var b;
    var c="99";
    if(a.length==12){
        b=a.substring(7,8);
        for(var i=0;i<code.length;i++){
            if(a==code[i]){
                c="10";
                break;
            }
        }
        if (c == "99") {  //增加判断，判断是否为新版电子票
            if (a.charAt(0) == '0' && a.substring(10,12) == '11') {
                c="10";
            }
            if (a.charAt(0) == '0' && (a.substring(10,12) == '06' || a.substring(10,12) == '07')) {  //判断是否为卷式发票  第1位为0且第11-12位为06或07
                c="11";
            }
        }
        if(c=="99"){ //如果还是99，且第8位是2，则是机动车发票
            if (b==2 && a.charAt(0) != '0') {
                c="03";
            }
        }
    }else if(a.length==10){
        b=a.substring(7,8);
        if(b==1||b==5){
            c="01";
        }else if(b==6||b==3){
            c="04";
        }else if(b==7||b==2){
            c="02";
        }
    }
    return c;
};

var getSwjg = function(fpdm, ckflag){
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 获取发票所属税务局信息
     * @Time: 2017/10/17 15:33
     * @param fpdm 发票代码
     * @param ckflag check标识
     * @Return: 发票所属税务局信息，包含地区名和ip
     */
    var citys=[{'code':'1100','sfmc':'北京','Ip':'https://zjfpcyweb.bjsat.gov.cn:443','address':'https://zjfpcyweb.bjsat.gov.cn:443'},
        {'code':'1200','sfmc':'天津','Ip':'https://fpcy.tjsat.gov.cn:443','address':'https://fpcy.tjsat.gov.cn:443'},
        {'code':'1300','sfmc':'河北','Ip':'https://fpcy.he-n-tax.gov.cn:82','address':'https://fpcy.he-n-tax.gov.cn:82'},
        {'code':'1400','sfmc':'山西','Ip':'https://fpcy.tax.sx.cn:443','address':'https://fpcy.tax.sx.cn:443'},
        {'code':'1500','sfmc':'内蒙古','Ip':'https://fpcy.nm-n-tax.gov.cn:443','address':'https://fpcy.nm-n-tax.gov.cn:443'},
        {'code':'2100','sfmc':'辽宁','Ip':'https://fpcy.tax.ln.cn:443','address':'https://fpcy.tax.ln.cn:443'},
        {'code':'2102','sfmc':'大连','Ip':'https://fpcy.dlntax.gov.cn:443','address':'https://fpcy.dlntax.gov.cn:443'},
        {'code':'2200','sfmc':'吉林','Ip':'https://fpcy.jl-n-tax.gov.cn:4432','address':'https://fpcy.jl-n-tax.gov.cn:4432'},
        {'code':'2300','sfmc':'黑龙江','Ip':'https://fpcy.hl-n-tax.gov.cn:443','address':'https://fpcy.hl-n-tax.gov.cn:443'},
        {'code':'3100','sfmc':'上海','Ip':'https://fpcyweb.tax.sh.gov.cn:1001','address':'https://fpcyweb.tax.sh.gov.cn:1001'},
        {'code':'3200','sfmc':'江苏','Ip':'https://fpdk.jsgs.gov.cn:80','address':'https://fpdk.jsgs.gov.cn:80'},
        {'code':'3300','sfmc':'浙江','Ip':'https://fpcyweb.zjtax.gov.cn:443','address':'https://fpcyweb.zjtax.gov.cn:443'},
        {'code':'3302','sfmc':'宁波','Ip':'https://fpcy.nb-n-tax.gov.cn:443','address':'https://fpcy.nb-n-tax.gov.cn:443'},
        {'code':'3400','sfmc':'安徽','Ip':'https://fpcy.ah-n-tax.gov.cn:443','address':'https://fpcy.ah-n-tax.gov.cn:443'},
        {'code':'3500','sfmc':'福建','Ip':'https://fpcyweb.fj-n-tax.gov.cn:443','address':'https://fpcyweb.fj-n-tax.gov.cn:443'},
        {'code':'3502','sfmc':'厦门','Ip':'https://fpcy.xm-n-tax.gov.cn','address':'https://fpcy.xm-n-tax.gov.cn'},
        {'code':'3600','sfmc':'江西','Ip':'https://fpcy.jxgs.gov.cn:82','address':'https://fpcy.jxgs.gov.cn:82'},
        {'code':'3700','sfmc':'山东','Ip':'https://fpcy.sd-n-tax.gov.cn:443','address':'https://fpcy.sd-n-tax.gov.cn:443'},
        {'code':'3702','sfmc':'青岛','Ip':'https://fpcy.qd-n-tax.gov.cn:443','address':'https://fpcy.qd-n-tax.gov.cn:443'},
        {'code':'4100','sfmc':'河南','Ip':'https://fpcy.ha-n-tax.gov.cn','address':'https://fpcy.ha-n-tax.gov.cn'},
        {'code':'4200','sfmc':'湖北','Ip':'https://fpcy.hb-n-tax.gov.cn:443','address':'https://fpcy.hb-n-tax.gov.cn:443'},
        {'code':'4300','sfmc':'湖南','Ip':'https://fpcy.hntax.gov.cn:8083','address':'https://fpcy.hntax.gov.cn:8083'},
        {'code':'4400','sfmc':'广东','Ip':'https://fpcy.gd-n-tax.gov.cn:443','address':'https://fpcy.gd-n-tax.gov.cn:443'},
        {'code':'4403','sfmc':'深圳','Ip':'https://fpcy.szgs.gov.cn:443','address':'https://fpcy.szgs.gov.cn:443'},
        {'code':'4500','sfmc':'广西','Ip':'https://fpcy.gxgs.gov.cn:8200','address':'https://fpcy.gxgs.gov.cn:8200'},
        {'code':'4600','sfmc':'海南','Ip':'https://fpcy.hitax.gov.cn:443','address':'https://fpcy.hitax.gov.cn:443'},
        {'code':'5000','sfmc':'重庆','Ip':'https://fpcy.cqsw.gov.cn:80','address':'https://fpcy.cqsw.gov.cn:80'},
        {'code':'5100','sfmc':'四川','Ip':'https://fpcy.sc-n-tax.gov.cn:443','address':'https://fpcy.sc-n-tax.gov.cn:443'},
        {'code':'5200','sfmc':'贵州','Ip':'https://fpcy.gz-n-tax.gov.cn:80','address':'https://fpcy.gz-n-tax.gov.cn:80'},
        {'code':'5300','sfmc':'云南','Ip':'https://fpcy.yngs.gov.cn:443','address':'https://fpcy.yngs.gov.cn:443'},
        {'code':'5400','sfmc':'西藏','Ip':'https://fpcy.xztax.gov.cn:81','address':'https://fpcy.xztax.gov.cn:81'},
        {'code':'6100','sfmc':'陕西','Ip':'https://fpcyweb.sn-n-tax.gov.cn:443','address':'https://fpcyweb.sn-n-tax.gov.cn:443'},
        {'code':'6200','sfmc':'甘肃','Ip':'https://fpcy.gs-n-tax.gov.cn:443','address':'https://fpcy.gs-n-tax.gov.cn:443'},
        {'code':'6300','sfmc':'青海','Ip':'https://fpcy.qh-n-tax.gov.cn:443','address':'https://fpcy.qh-n-tax.gov.cn:443'},
        {'code':'6400','sfmc':'宁夏','Ip':'https://fpcy.nxgs.gov.cn:443','address':'https://fpcy.nxgs.gov.cn:443'},
        {'code':'6500','sfmc':'新疆','Ip':'https://fpcy.xj-n-tax.gov.cn:443','address':'https://fpcy.xj-n-tax.gov.cn:443'}];
    var dqdm=null;
    var swjginfo=new Array();
    if(fpdm.length==12){//发票代码为12位时，地区代码为2-5位，否则地区代码为前四位
        dqdm=fpdm.substring(1,5);
    }else{
        dqdm=fpdm.substring(0,4);
    }
    if(dqdm!="2102"&&dqdm!="3302"&&dqdm!="3502"&&dqdm!="3702"&&dqdm!="4403"){//除开大连，青岛，宁波，厦门，深圳五座计划单列市，其他城市地区代码为地区代码前两位+00
        dqdm=dqdm.substring(0,2)+"00";
    }
    for(var i=0;i<citys.length;i++){
        if(dqdm==citys[i].code){
            swjginfo[0]=citys[i].sfmc;
            if (flag == 'debug') {   //如果是开发调试模式或测试模式
                //swjginfo[1] = "http://172.30.11.88:7010/WebQuery";  //这里是省局服务器的外网地址，开发/测试时填写相应值
                swjginfo[1] = "http://127.0.0.1:7001/WebQuery";
            } else {
                swjginfo[1]=citys[i].Ip+"/WebQuery";
                swjgmc = swjginfo[0];
            }
            break;
        }
    }
    //只有北京，上海，深圳的发票可以查询  如果全国开放，此处加注释
    /*
    if ((fpdm.length == 10 && fpdm.substring(0,1) != '0' && $.inArray(fpdm, code10) == -1) || fpdm.length == 12) {
      if (fpdm.substring(0,1) == '1' && (fpdm.substring(1,5) == '1100' || fpdm.substring(1,5) == '3100' || fpdm.substring(1,5) == '4403')) {
      } else {
        if (dqdm != "1100" && dqdm != "3100" && dqdm != "4403") {
            swjginfo = new Array();
            jAlert("该省尚未开通发票查验功能！","提示");
        }
      }
    }*/
    return swjginfo;
};

var avai = function(fplx){
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 对开具金额字段进行处理
     * @Time: 2017/10/17 16:27
     * @param fplx 发票类型
     * @Return:
     */
    if (fplx == "01" || fplx == "02" || fplx == "03") {//只有这三种发票kjje字段为金额，区域发票kjje字段为校验码后六位，"01"表示增值税专票
        var index = kjje.indexOf(".");
        if (index > 0) {
            var arr = kjje.split(".");
            if (arr[1] == "00" || arr[1] == "0") {//去掉末位的0
                kjje = arr[0];
            } else if (arr[1].charAt(1) == "0") {
                kjje = arr[0] + "." + arr[1].charAt(0);
            }
        }
    }
};

var getErrorMsg1 = function(k) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 获取验证码请求返回的错误信息
     * @Time: 2017/10/17 16:35
     * @param k 错误代码
     * @Return: 错误信息
     */
    var code_msg = {
        "003": "验证码请求次数过于频繁，请1分钟后再试！",
        "005": "非法请求!",
        "010": "网络超时，请重试！(01)",
        "fpdmerr": "请输入合法发票代码!",
        "024": "24小时内验证码请求太频繁，请稍后再试！",
        "016": "服务器接收的请求太频繁，请稍后再试！",
        "020": "由于查验行为异常，涉嫌违规，当前无法使用查验服务！"
    };
    if(k in code_msg){
        status1 = false;
        ErrorMsg1 = code_msg[k];
    }else if(k != ""){
        status1 = true;
        ErrorMsg1 = null;
    }else{
        status1 = false;
        ErrorMsg1 = "未知错误";
    }
}

function showTime() {
    var myDate = new Date();
    var time=myDate.getTime();
    return time;
}