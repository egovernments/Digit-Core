const get = require("lodash.get");


function rupeeFormat(amount) {
    flag = 0;
    var count = 0;
    var amount = amount.toString();
    var len = amount.length;
    var val = '';
    for (var i = len - 1; i >= 0; i--) {
        val = amount[i] + val;
        if (amount[i] == '.') {
            flag = 1;
            count = -1;
            val = amount.substring(i, len)
        };
        if (count % 2 == 0 && i != 0 && count != 0) {
            val = ',' + val;
        };
        count += 1;
    };
    // if (flag == 0) { val += '.00' };
    return val
};

let calculateAttendenceDetails = (musterIndvEntries, estimateCalc, labourCharges, localizationMap) => {
    var y = [];
    let calcDetails = get(estimateCalc, 'estimates[0].calcDetails', [])
    var estIdentifierMap = {}
    calcDetails.forEach(element => {
        if (element?.payee?.identifier) {
            estIdentifierMap[element.payee.identifier] = element;
        }
    })
    for (var i = 0; i < musterIndvEntries.length; i++) {
        
        function color(attendance) {
            return attendance == 'F' ? '#095729' : attendance == 'H' ? '#F6B458' : '#ff0000';
        }

        function getAttendance(attendance) {
            var half_attendance = 'H';
            var full_attendance = 'F';
            var absent = 'A';
            return attendance == 1 ? full_attendance : attendance == 0 ? absent : half_attendance;
        }

        let musterEntity = musterIndvEntries[i];
        let additionalDetails = musterIndvEntries[i]?.additionalDetails || {};
        // Sort by descending order
        musterEntity.attendanceEntries = musterEntity.attendanceEntries.sort((a, b) => parseFloat(b.time) - parseFloat(a.time));
        var attendance_of_sun = getAttendance(musterEntity.attendanceEntries[0].attendance);
        var attendance_of_sat = getAttendance(musterEntity.attendanceEntries[1].attendance);
        var attendance_of_fri = getAttendance(musterEntity.attendanceEntries[2].attendance);
        var attendance_of_thu = getAttendance(musterEntity.attendanceEntries[3].attendance);
        var attendance_of_wed = getAttendance(musterEntity.attendanceEntries[4].attendance);
        var attendance_of_tue = getAttendance(musterEntity.attendanceEntries[5].attendance);
        var attendance_of_mon = getAttendance(musterEntity.attendanceEntries[6].attendance);
        
        let perDayWage = (additionalDetails?.skillCode && labourCharges[additionalDetails?.skillCode]) ? labourCharges[additionalDetails?.skillCode] : "";
        if (perDayWage && !isNaN(perDayWage)) perDayWage = rupeeFormat(perDayWage);

        let totalWage = get(estIdentifierMap, musterEntity.individualId + ".payableLineItem[0].amount", "");
        if (totalWage && !isNaN(totalWage)) totalWage = rupeeFormat(totalWage);

        y.push({
            'attendance_of_sun': attendance_of_sun,
            'attendance_of_sat': attendance_of_sat,
            'attendance_of_fri': attendance_of_fri,
            'attendance_of_thu': attendance_of_thu,
            'attendance_of_wed': attendance_of_wed,
            'attendance_of_tue': attendance_of_tue,
            'attendance_of_mon': attendance_of_mon,
            'bank_account_details': additionalDetails.bankDetails,
            'aadhar_number': additionalDetails?.aadharNumber,
            'skill': getLocalizedSkill(additionalDetails.skillCode, localizationMap),
            'background_color': i % 2 == 0 ? '' : '#eeeeee',
            'sl_no': i + 1,
            'name_of_the_individual': additionalDetails.userName,
            'guardian_name': additionalDetails.fatherName || "",
            'actualTotalAttendance': musterEntity.modifiedTotalAttendance == null ? musterEntity.actualTotalAttendance: musterEntity.modifiedTotalAttendance,
            'attendance': musterEntity.attendanceEntries[0].attendance,
            'sat_color_of_attendance': color(attendance_of_sat),
            'mon_color_of_attendance': color(attendance_of_mon),
            'tue_color_of_attendance': color(attendance_of_tue),
            'wed_color_of_attendance': color(attendance_of_wed),
            'thu_color_of_attendance': color(attendance_of_thu),
            'fri_color_of_attendance': color(attendance_of_fri),
            'sun_color_of_attendance': color(attendance_of_sun),
            'registration_id': additionalDetails.userId,
            'days_worked': musterEntity.actualTotalAttendance,
            'per_day_wage': perDayWage,
            'total_wage': totalWage
        });
    }
    return y;
}

let getLocalizedSkill = (skillCode, localizationMap) => {
    let prefix = "COMMON_MASTERS_SKILLS_";
    return localizationMap[prefix + skillCode] || skillCode;
}

let calculateAttendenceTotal = (musterIndvEntries, estimateCalc) => {
    var y = [];
    var total_day_worked = 0;
    var total_wage = 0;
    var sun_day_total_attendance = 0;
    var fri_day_total_attendance = 0;
    var mon_day_total_attendance = 0;
    var tue_day_total_attendance = 0;
    var wed_day_total_attendance = 0;
    var thu_day_total_attendance = 0;
    var thu_day_total_attendance = 0;
    var sat_day_total_attendance = 0;
    if (musterIndvEntries) {
        for (var i = 0; i < musterIndvEntries.length; i++) {
            // Sort by descending order
            musterIndvEntries[i].attendanceEntries = musterIndvEntries[i].attendanceEntries.sort((a, b) => parseFloat(b.time) - parseFloat(a.time));
            mon_day_total_attendance = mon_day_total_attendance + musterIndvEntries[i].attendanceEntries[6].attendance;
            tue_day_total_attendance = tue_day_total_attendance + musterIndvEntries[i].attendanceEntries[5].attendance;
            wed_day_total_attendance = wed_day_total_attendance + musterIndvEntries[i].attendanceEntries[4].attendance;
            thu_day_total_attendance = thu_day_total_attendance + musterIndvEntries[i].attendanceEntries[3].attendance;
            fri_day_total_attendance = fri_day_total_attendance + musterIndvEntries[i].attendanceEntries[2].attendance;
            sat_day_total_attendance = sat_day_total_attendance + musterIndvEntries[i].attendanceEntries[1].attendance;
            sun_day_total_attendance = sun_day_total_attendance + musterIndvEntries[i].attendanceEntries[0].attendance;
            total_day_worked += musterIndvEntries[i].actualTotalAttendance;
        };
        total_wage = estimateCalc?.totalAmount
        total_wage = rupeeFormat(total_wage);
        y.push({
            'mon_day_total_attendance': mon_day_total_attendance,
            'tue_day_total_attendance': tue_day_total_attendance,
            'wed_day_total_attendance': wed_day_total_attendance,
            'thu_day_total_attendance': thu_day_total_attendance,
            'fri_day_total_attendance': fri_day_total_attendance,
            'sat_day_total_attendance': sat_day_total_attendance,
            'sun_day_total_attendance': sun_day_total_attendance,
            'total_day_worked': total_day_worked, 
            'total_wage': total_wage
        })
    } return y;

}

let getDateMonth = (timeStamp) => {
    var y;
    var month='';
    var date= new Date(timeStamp).toLocaleString('en-US', { timeZone: 'Asia/Kolkata' }).split("/"); // It returns the time in format '5/14/2023, 12:00:00 AM'
    if(date[0]=='1'){month='Jan'};
    if(date[0]=='2'){month='feb'};
    if(date[0]=='3'){month='Mar'};
    if(date[0]=='4'){month='Apr'};
    if(date[0]=='5'){month='May'};
    if(date[0]=='6'){month='Jun'};
    if(date[0]=='7'){month='Jul'};
    if(date[0]=='8'){month='Aug'};
    if(date[0]=='9'){month='Sep'};
    if(date[0]=='10'){month='Oct'};
    if(date[0]=='11'){month='Nov'};
    if(date[0]=='12'){month='Dec'};
    y = date[1]+' '+month;
    return y;
}

module.exports = {
    calculateAttendenceDetails, 
    calculateAttendenceTotal,
    getDateMonth
}