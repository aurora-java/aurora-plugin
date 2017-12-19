/**
 * @Author: xuzhao
 * @Email: mailto:zhao.xu@hand-china.com
 * @Description: 响应结果处理
 * @Time: 2017/10/17 19:06
 */
var handleMsg = function(k) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 处理请求返回的错误信息
     * @Time: 2017/10/17 19:07
     * @param k 错误代码
     * @Return: 错误信息
     */
    var code_msg = {
        "1": "该省尚未开通发票查验功能!",
        "002": "超过该张发票当日查验次数(请于次日再次查验)!",
        "003": "发票查验请求太频繁，请稍后再试!",
        "004": "超过服务器最大请求数，请稍后访问!",
        "005": "非法请求!",
        "006": "不一致!",
        "007": "验证码失效!",
        "008": "验证码错误!",
        "009": "查无此票!",
        "rqerr": "当日开具发票可于次日进行查验!",
        "003": "验证码请求次数过于频繁，请1分钟后再试!",
        "005": "非法请求!",
        "010": "网络超时，请重试!",
        "010_": "网络超时，请重试!",
        "fpdmerr": "请输入合法发票代码!",
        "024": "24小时内验证码请求太频繁，请稍后再试!",
        "016": "服务器接收的请求太频繁，请稍后再试!",
        "020": "由于查验行为异常，涉嫌违规，当前无法使用查验服务!"
    };
    if(k in code_msg){
        if(k === "007" || k === "008"){
            status = "0";
        }else{
            status = "-1";
        }
        errorMsg = {
            "errorCode": k,
            "errorMsg": code_msg[k]
        };
    }else if(k != ""){
        status = "1";
        errorMsg = {
            "errorCode": '',
            "errorMsg": ''
        };
    }else{
        status = "-1";
        errorMsg = {
            "errorCode": "xxx",
            "errorMsg": "未知错误"
        };
    }
        errorMsg = JSON.stringify(errorMsg);
};

var FormatDate = function (time, add){
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 解密日期
     * @Time: 2017/10/18 10:08
     * @param time 加密的时间
     * @param add 解密的key
     * @Return: 解密后的日期
     */
    var year=time.substring(0,4);
    var month=parseInt(time.substring(4,6), 10);
    var day=parseInt(time.substring(6), 10);
    var d = new Date(year + "/" + month + "/" + day);
    d.setDate(d.getDate() + (0 - add));
    var s = d.getFullYear() + "年" + ((d.getMonth() + 1) > 9 ? (d.getMonth() + 1) : "0" + (d.getMonth() + 1)) + "月" + (d.getDate() > 9 ? d.getDate() : "0" + d.getDate()) + "日";
    return s;
};

var FormatSBH = function (sbh, str) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 解密纳税人识别号
     * @Time: 2017/10/18 10:16
     * @param 加密sbh 纳税人识别号
     * @param str 解密的key
     * @Return: 解密后的纳税人识别号
     */
    var s1 = str.split("_");
    for (var i = 0; i < s1.length; i++) {
        sbh = chgchar(sbh, s1[i]);
    }
    return sbh;
};

var chgchar = function (nsrsbh, ss) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 纳税人识别号的解密逻辑
     * @Time: 2017/10/18 10:21
     * @param nsrsbh 加密的纳税人识别号
     * @param ss 解密的key
     * @Return: 解密的正确结果
     */
    var a = ss.charAt(2);
    var b = ss.charAt(0);  //反向替换，所以和java中是相反的
    nsrsbh = nsrsbh.replaceAll(a, '#');
    nsrsbh = nsrsbh.replaceAll(b, '%');
    nsrsbh = nsrsbh.replaceAll('#', b);
    nsrsbh = nsrsbh.replaceAll('%', a);
    return nsrsbh;
};

var getje = function (je, ss) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description:
     * @Time: 2017/10/18 10:24
     * @param je 加密的金额
     * @param ss 解密的key
     * @Return: 解密后的金额
     */
    if (typeof(je) != "undefined" && je != "") {
        return accAdd(je, ss);
    } else {
        return je;
    }
    //return je;
};

var accAdd = function (arg1, arg2) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 金额的解密逻辑
     * @Time: 2017/10/18 10:25
     * @param arg1 加密的金额
     * @param arg2 解密的key
     * @Return: 解密后的结果
     */
    var r1,r2,m;
    if (arg1.trim() == "") {
        return arg1;
    }
    if(parseInt(arg1, 10)==arg1){
        r1=0;
    }else{
        r1=arg1.toString().split(".")[1].length;
    }
    if(parseInt(arg2, 10)==arg2){
        r2=0;
    }else{
        r2=arg2.toString().split(".")[1].length;
    }
    m = Math.pow(10, Math.max(r1, r2))  ;
    //alert(m);
    var r = (arg1 * m + arg2 * m) / m  ;
    return r.toFixed(2);
};

var NoToChinese = function (currencyDigits, fplx) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 将金额小写改为大写
     * @Time: 2017/10/18 10:28
     * @param currencyDigits 金额
     * @param fplx 发票类型
     * @Return: 金额的大写
     */
//	Constants:
    var MAXIMUM_NUMBER = 99999999999.99;
    // Predefine the radix characters and currency symbols for output:
    var CN_ZERO = "零";
    var CN_ONE = "壹";
    var CN_TWO = "贰";
    var CN_THREE = "叁";
    var CN_FOUR = "肆";
    var CN_FIVE = "伍";
    var CN_SIX = "陆";
    var CN_SEVEN = "柒";
    var CN_EIGHT = "捌";
    var CN_NINE = "玖";
    var CN_TEN = "拾";
    var CN_HUNDRED = "佰";
    var CN_THOUSAND = "仟";
    var CN_TEN_THOUSAND = "万";
    var CN_HUNDRED_MILLION = "亿";
    var CN_SYMBOL = "";
    var CN_DOLLAR = "圆";
    var CN_TEN_CENT = "角";
    var CN_CENT = "分";
    var CN_INTEGER = "整";
    if (fplx == "02" || fplx == "03") {
        CN_DOLLAR = "元";
    }

//	Variables:
    var integral;    // Represent integral part of digit number.
    var decimal;    // Represent decimal part of digit number.
    var outputCharacters;    // The output result.
    var parts;
    var digits, radices, bigRadices, decimals;
    var zeroCount;
    var i, p, d;
    var quotient, modulus;

//	Validate input string:
    currencyDigits = currencyDigits.toString();
    if (currencyDigits.trim() == "") {
        //alert("请输入小写金额！");
        return "";
    }
    if (currencyDigits.match(/[^,.\d]/) != null) {
        if (currencyDigits.substring(0,1) != '-') {
            alert("小写金额含有无效字符！");
            return "";
        }
    }
    if ((currencyDigits).match(/^((\d{1,3}(,\d{3})*(.((\d{3},)*\d{1,3}))?)|(\d+(.\d+)?))$/) == null) {
        if (currencyDigits.substring(0,1) != '-') {
            alert("小写金额的格式不正确！");
            return "";
        }
    }
    var fushuflag = "";
    if (currencyDigits.substring(0,1) == '-') {
        if (fplx == "01" || fplx == "04") {
            fushuflag = "（负数）";
        } else if (fplx == "02" || fplx == "03" || fplx == "11") {
            fushuflag = "负数：";
        } else if (fplx == "10") {
            fushuflag = "负";
        } else {
            fushuflag = "（负数）";
        }

        currencyDigits = currencyDigits.substring(1, currencyDigits.length);
    }
//	Normalize the format of input digits:
    currencyDigits = currencyDigits.replace(/,/g, "");    // Remove comma delimiters.
    currencyDigits = currencyDigits.replace(/^0+/, "");    // Trim zeros at the beginning.
    // Assert the number is not greater than the maximum number.
    if (Number(currencyDigits) > MAXIMUM_NUMBER) {
        alert("金额过大，应小于1000亿元！");
        return "";
    }

//	Process the coversion from currency digits to characters:
    // Separate integral and decimal parts before processing coversion:
    parts = currencyDigits.split(".");
    if (parts.length > 1) {
        integral = parts[0];
        decimal = parts[1];
        // Cut down redundant decimal digits that are after the second.
        decimal = decimal.substr(0, 2);
    }
    else {
        integral = parts[0];
        decimal = "";
    }
    // Prepare the characters corresponding to the digits:
    digits = new Array(CN_ZERO, CN_ONE, CN_TWO, CN_THREE, CN_FOUR, CN_FIVE, CN_SIX, CN_SEVEN, CN_EIGHT, CN_NINE);
    radices = new Array("", CN_TEN, CN_HUNDRED, CN_THOUSAND);
    bigRadices = new Array("", CN_TEN_THOUSAND, CN_HUNDRED_MILLION);
    decimals = new Array(CN_TEN_CENT, CN_CENT);
    // Start processing:
    outputCharacters = "";
    // Process integral part if it is larger than 0:
    if (Number(integral) > 0) {
        zeroCount = 0;
        for (i = 0; i < integral.length; i++) {
            p = integral.length - i - 1;
            d = integral.substr(i, 1);
            quotient = p / 4;
            modulus = p % 4;
            if (d == "0") {
                zeroCount++;
            }
            else {
                if (zeroCount > 0)
                {
                    outputCharacters += digits[0];
                }
                zeroCount = 0;
                outputCharacters += digits[Number(d)] + radices[modulus];
            }
            if (modulus == 0 && zeroCount < 4) {
                outputCharacters += bigRadices[quotient];
                zeroCount = 0;
            }
        }
        outputCharacters += CN_DOLLAR;
    }
    // Process decimal part if there is:
    if (decimal != "") {
        for (i = 0; i < decimal.length; i++) {
            d = decimal.substr(i, 1);
            if (d != "0") {
                outputCharacters += digits[Number(d)] + decimals[i];
            }
        }
    }
    // Confirm and return the final output string:
    if (outputCharacters == "") {
        outputCharacters = CN_ZERO + CN_DOLLAR;
    }
    if (decimal == "" || decimal == "00" || decimal == "0" ) {
        outputCharacters += CN_INTEGER;
    }
    outputCharacters = fushuflag + CN_SYMBOL + outputCharacters;
    return outputCharacters;
};

var hwxxs, fpxxs;//货物信息和发票信息

var decryptFpyzResponseResult = function (data) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 对发票验证请求返回的结果进行解析
     * @Time: 2017/10/18 10:42
     * @param data 响应结果
     * @Return:
     */
    var tempno = data.template;
    if (tempno == 0) {
        fplx=data.fplx;
        hwxxs=data.hwxx;
        fpxxs=data.fpxx;
    } else if (tempno == 1) {
        fplx=data.f3ld;
        hwxxs=data.fdzx;
        fpxxs=data.h2gx;
    } else if (tempno == 2) {
        fplx=data.a3b0;
        hwxxs=data.eb2a;
        fpxxs=data.f8d7;
    } else if (tempno == 3) {
        fplx=data.c342;
        hwxxs=data.dbd2;
        fpxxs=data.d64b;
    } else if (tempno == 4) {
        fplx=data.af0b;
        hwxxs=data.c32a;
        fpxxs=data.a22a;
    } else if (tempno == 5) {
        fplx=data.ecae;
        hwxxs=data.c3c0;
        fpxxs=data.cb20;
    } else if (tempno == 6) {
        fplx=data.c3c8;
        hwxxs=data.a574;
        fpxxs=data.da20;
    } else if (tempno == 7) {
        fplx=data.dc02;
        hwxxs=data.cc66;
        fpxxs=data.ddbb;
    } else if (tempno == 8) {
        fplx=data.b3dd;
        hwxxs=data.c2b9;
        fpxxs=data.e72d;
    } else if (tempno == 9) {
        fplx=data.f16a;
        hwxxs=data.ceb5;
        fpxxs=data.a83e;
    }
    var splitstr = rules[0];
    fpxxs = fpxxs.replaceAll(splitstr, "≡");
    hwxxs = hwxxs.replaceAll(splitstr, "≡");
    splitstr = "≡";
    var sort = data.sort;
    var sortarray = sort.split("_");
    var tmpfpxx = fpxxs.split("≡");
    var cysj = tmpfpxx[tmpfpxx.length - 1] ;
    var tmpfp = new Array(tmpfpxx.length - 4);
    for (i = 3; i < tmpfpxx.length - 1; i++) {
        tmpfp[i - 3] = tmpfpxx[i];
    }
    var newfpxx = new Array(tmpfpxx.length - 4);
    for (i = 0; i < tmpfpxx.length - 4; i++) {
        newfpxx[i] = tmpfp[parseInt(sortarray[i])];
    }
    var newfpxxstr = tmpfpxx[0] + "≡" + tmpfpxx[1] + "≡" + tmpfpxx[2] + "≡";
    for (i = 0; i < newfpxx.length; i++) {
        newfpxxstr = newfpxxstr + newfpxx[i] + "≡";
    }
    fpxxs = newfpxxstr + cysj;
    if(fpxxs!=null&&fpxxs!="") {
        fpxx = fpxxs.split(splitstr); //"≡");
    }
};

var GetJeToDot = function (je) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 获取带小数点的金额
     * @Time: 2017/10/18 10:46
     * @param je 金额
     * @Return: 带小数点的金额
     */
    if (typeof(je) != "undefined" && je.trim() != ""){
        if (je.trim() == '-') {
            return je;
        }
        je = je.trim() + "";
        if (je.substring(0, 1) == '.') {
            je = '0' + '.' + je.substring(1, je.length);
            return je;
        }
        var index=je.indexOf(".");
        if(index<0){
            je+=".00";
        }else if(je.split(".")[1].length==1){
            je+="0";
        }
        if (je.substring(0,2) == '-.') {
            je = '-0.' + je.substring(2, je.length);
        }
        return je;
    } else {
        return je;
    }
};

var getzeroDot = function (je) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 获取整数部分为0的带小数点的金额
     * @Time: 2017/10/18 10:50
     * @param je 金额
     * @Return: 整数部分为0的带小数点的金额
     */
    if (je.substring(0, 2) == "-.") {
        je = "-0." + je.substring(2);
    } else if (je.substring(0, 1) == ".") {
        je = "0." + je.substring(1);
    }
    return je;
};

var FormatHwmc = function (mc, str) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 解密货物名称
     * @Time: 2017/10/18 10:52
     * @param mc 加密的货物名称
     * @param str 解密的密钥
     * @Return: 解密后的货物名称
     */
    var ss = mc.replaceAll(str, "");
    return ss;
};

var FormatSl = function (data) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 格式化税率字段
     * @Time: 2017/10/18 10:57
     * @param data 税率
     * @Return: 正确格式的税率
     */
    data = data.trim();
    if(data.substring(0,1)=="."){
        data=parseFloat("0"+data)*100;
    }
    if (data.length > 0) {
        return data+"%";
    } else {
        return "";
    }
};

var getFplx = function (fplx) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 获取发票类型
     * @Time: 2017/10/18 10:58
     * @param fplx 发票类型代码
     * @Return: 发票类型名
     */
    var fplxName ="增值税普通发票";
    if(fplx == "10"){
        fplxName = "增值税电子普通发票";
    }else if(fplx == "01"){
        fplxName = "增值税专用发票";
    }
    return fplxName;
};

var getInvoiceCheckResult = function (fpxx, hwxxs) {
    /**
     * @Author: xuzhao
     * @Email: mailto:zhao.xu@hand-china.com
     * @Description: 获取发票验证结果
     * @Time: 2017/10/18 14:03
     * @param fpxx 发票信息数组
     * @param hwxxs 货物信息
     * @Return: 验证结果json字符串
     */
    /***************解析发票信息开始****************/
    var header_information = null;
    if(fplx == "10") {
        header_information = {
            // "查验次数": Number(fpxx[3]) + 1,
            // "查验时间": fpxx[20],
            "invoice_code": fpxx[0].trim(),
            "invoice_number": fpxx[1].trim(),
            "invoice_distrect": fpxx[2].trim(),
            "invoice_type": getFplx(fplx).trim(),
            "invoice_date": FormatDate(fpxx[4], rules[3]).trim(),
            "machine_number": fpxx[17].trim(),
            "check_code": fpxx[13].trim(),
            "purchaser_name": fpxx[9].trim(),
            "purchaser_tax_number": FormatSBH(fpxx[10], rules[1]).trim(),
            "purchaser_address_phone": fpxx[11].trim(),
            "purchaser_bank_account": fpxx[12].trim(),
            "amount": GetJeToDot(getje(fpxx[15], rules[2])).trim(),
            "amount_zhs": '⊗'+ NoToChinese(GetJeToDot(getje(fpxx[15], rules[2])),fplx).trim(),
            "without_tax_amount": GetJeToDot(getje(fpxx[18], rules[2])).trim(),
            "tax_amount": GetJeToDot(getje(fpxx[14], rules[2])).trim(),
            "seller_name": fpxx[5].trim(),
            "seller_tax_number": fpxx[6].trim(),
            "seller_address_phone": fpxx[7].trim(),
            "seller_bank_account": fpxx[8].trim(),
            "remark": jmbz.replace(/\r\n/g, "<br/>").replace(/\n/g, "<br/>").trim()
        };
//		seller_information = {
//		"seller_name": fpxx[5],
//		"seller_tax_number": fpxx[6],
//		"seller_address_phone": fpxx[7],
//		"seller_bank_account": fpxx[8]
//		};
//		purchaser_information = {
//		"purchaser_name": fpxx[9],
//		"purchaser_tax_number": FormatSBH(fpxx[10], rules[1]),
//		"purchaser_address_phone": fpxx[11],
//		"purchaser_bank_account": fpxx[12]
//		};
    }else if(fplx == "04"){
        header_information = {
            // "查验次数": Number(fpxx[3]) + 1,
            // "查验时间": fpxx[20],
            "invoice_code": fpxx[0].trim(),
            "invoice_number": fpxx[1].trim(),
            "invoice_distrect": fpxx[2].trim(),
            "invoice_type": getFplx(fplx).trim(),
            "invoice_date": FormatDate(fpxx[4], rules[3]).trim(),
            "machine_number": fpxx[17].trim(),
            "check_code": fpxx[13].trim(),
            "purchaser_name": fpxx[9].trim(),
            "purchaser_tax_number": FormatSBH(fpxx[10], rules[1]).trim(),
            "purchaser_address_phone": fpxx[11].trim(),
            "purchaser_bank_account": fpxx[12].trim(),
            "amount": GetJeToDot(getje(fpxx[15], rules[2])).trim(),
            "amount_zhs": '⊗'+ NoToChinese(GetJeToDot(getje(fpxx[15], rules[2])),fplx).trim(),
            "without_tax_amount": GetJeToDot(getje(fpxx[19], rules[2])).trim(),
            "tax_amount": GetJeToDot(getje(fpxx[14], rules[2])).trim(),
            "seller_name": fpxx[5].trim(),
            "seller_tax_number": fpxx[6].trim(),
            "seller_address_phone": fpxx[7].trim(),
            "seller_bank_account": fpxx[8].trim(),
            "remark": jmbz.replace(/\r\n/g, "<br/>").replace(/\n/g, "<br/>").trim()
        };
//		seller_information = {
//		"seller_name": fpxx[5],
//		"seller_tax_number": fpxx[6],
//		"seller_address_phone": fpxx[7],
//		"seller_bank_account": fpxx[8]
//		};
//		purchaser_information = {
//		"purchaser_name": fpxx[9],
//		"purchaser_tax_number": FormatSBH(fpxx[10], rules[1]),
//		"purchaser_address_phone": fpxx[11],
//		"purchaser_bank_account": fpxx[12]
//		};
    }else{
        header_information = {
            // "查验次数": Number(fpxx[3]) + 1,
            // "查验时间": fpxx[21],
            "invoice_code": fpxx[0].trim(),
            "invoice_number": fpxx[1].trim(),
            "invoice_distrect": fpxx[2].trim(),
            "invoice_type": getFplx(fplx).trim(),
            "invoice_date": FormatDate(fpxx[4], rules[3]).trim(),
            "machine_number": fpxx[17].trim(),
            "check_code": fpxx[19].trim(),
            "purchaser_name": fpxx[5].trim(),
            "purchaser_tax_number": fpxx[6].trim(),
            "purchaser_address_phone": fpxx[7].trim(),
            "purchaser_bank_account": fpxx[8].trim(),
            "amount": GetJeToDot(getje(fpxx[15], rules[2])).trim(),
            "amount_zhs": '⊗'+ NoToChinese(GetJeToDot(getje(fpxx[15], rules[2])),"01").trim(),
            "without_tax_amount": GetJeToDot(getje(fpxx[13], rules[2])).trim(),
            "tax_amount": GetJeToDot(getje(fpxx[14], rules[2])).trim(),
            "seller_name": fpxx[9].trim(),
            "seller_tax_number": FormatSBH(fpxx[10], rules[1]).trim(),
            "seller_address_phone": fpxx[11].trim(),
            "seller_bank_account": fpxx[12].trim(),
            "remark": jmbz.replace(/\r\n/g, "<br/>").replace(/\n/g, "<br/>").trim()
        };
//		purchaser_information = {
//		"purchaser_name": fpxx[5],
//		"purchaser_tax_number": fpxx[6],
//		"purchaser_address_phone": fpxx[7],
//		"purchaser_bank_account": fpxx[8]
//		};
//		seller_information = {
//		"seller_name": fpxx[9],
//		"seller_tax_number": FormatSBH(fpxx[10], rules[1]),
//		"seller_address_phone": fpxx[11],
//		"seller_bank_account": fpxx[12]
//		};
    }
    /***************解析发票信息结束****************/
    /***************解析货物信息开始****************/
    var hwstr = rules[4],
        je = rules[2],
        items = {

        },
        hwii = hwxxs.split("▄");
    if(hwii.length > 1){
        sechw = hwii[1];
        var hwinfo = sechw.split('▎'),
            hw,
            item = null;
        if(hwinfo !=""){
            for(var i = 0; i < hwinfo.length; i++){
                hw = hwinfo[i].split('█');
                if(fplx == "10"){
                    item ={
                        "goods_or_taxable_service": FormatHwmc(hw[0], hwstr).trim(),
                        "specifications": hw[1].trim(),
                        "unit": hw[2].trim(),
                        "quantity": getzeroDot(hw[6]).trim(),
                        "unit_price": GetJeToDot(hw[4], je).trim(),
                        "without_tax_amount": GetJeToDot(hw[5], je).trim(),
                        "tax_rate": FormatSl(hw[3]).trim(),
                        "tax_amount": GetJeToDot(hw[7], je).trim()
                    };
                    items[i+1] = item;
                }else{
                    item ={
                        "goods_or_taxable_service": FormatHwmc(hw[0], hwstr).trim(),
                        "specifications": hw[1].trim(),
                        "unit": hw[2].trim(),
                        "quantity": getzeroDot(hw[3]).trim(),
                        "unit_price": GetJeToDot(hw[4], je).trim(),
                        "without_tax_amount": GetJeToDot(hw[5], je).trim(),
                        "tax_rate": FormatSl(hw[6]).trim(),
                        "tax_amount": GetJeToDot(hw[7], je).trim()
                    };
                    items[i+1] = item;
                }
            }
        }
    }else{
        var hwinfo = hwxxs.split('≡'),
            hw;
        for(var i = 0; i < hwinfo.length; i++){
            var item = null;
            hw = hwinfo[i].split('█');
            if(fplx == "10"){
                item = {
                    "goods_or_taxable_service": FormatHwmc(hw[0], hwstr).trim(),
                    "specifications": hw[1].trim(),
                    "unit": hw[2].trim(),
                    "quantity": getzeroDot(hw[6]).trim(),
                    "unit_price": GetJeToDot(hw[4]).trim(),
                    "without_tax_amount": GetJeToDot(hw[5]).trim(),
                    "tax_rate": FormatSl(hw[3]).trim(),
                    "tax_amount": GetJeToDot(hw[7]).trim()
                };
                items[i+1] = item;
            }else{
                item = {
                    "goods_or_taxable_service": FormatHwmc(hw[0], hwstr).trim(),
                    "specifications": hw[1].trim(),
                    "unit": hw[2].trim(),
                    "quantity": getzeroDot(hw[3]).trim(),
                    "unit_price": GetJeToDot(hw[4]).trim(),
                    "without_tax_amount": GetJeToDot(hw[5]).trim(),
                    "tax_rate": FormatSl(hw[6]).trim(),
                    "tax_amount": GetJeToDot(hw[7]).trim()
                };
                items[i+1] = item;
            }
        }
    }
    /***************解析货物信息结束****************/
    var result = {
        "header_information": header_information,
        "lines_information": items,
    };
    return JSON.stringify(result);
}

String.prototype.replaceAll = function(reallyDo, replaceWith, ignoreCase) {
    if (!RegExp.prototype.isPrototypeOf(reallyDo)) {
        return this.replace(new RegExp(reallyDo, (ignoreCase ? "gi": "g")), replaceWith);
    } else {
        return this.replace(reallyDo, replaceWith);
    }
}