var createError = require("http-errors");
var express = require("express");
var path = require("path");
var cookieParser = require("cookie-parser");
var logger = require("morgan");
var config = require("./config");
var projectsRouter=require("./routes/projects");
var estimateRouter=require("./routes/estimate");
var musterRollRouter = require("./routes/musterRolls");
var workOrderRouter = require("./routes/workOrder");
var groupBills = require("./routes/groupBill");
const deviationStatementRouter = require("./routes/deviationStatement");
const measurementBookRouter = require("./routes/measurementBook");
const detailedEstimateRouter = require("./routes/detailedEstimate");
const issueOfSummon = require("./routes/issueOfSummon")
const rescheduleRequestJudge = require("./routes/rescheduleRequestJudge");
const newHearingDateAfterReschedule = require("./routes/newHearingDateAfterRescheduling");
const scheduleHearingDate =require("./routes/scheduleHearingDate");
const acceptReschedulingRequest=require("./routes/acceptReschedulingRequest");
const rejectReschedulingRequest=require("./routes/rejectReschedulingRequest");
const acceeptAdrApplication = require("./routes/acceptAdrApplication");
const rejectAdrApplication = require("./routes/rejectAdrApplication");

// var {listenConsumer} = require("./consumer")



var app = express();
app.disable('x-powered-by');

// view engine setup
app.set("views", path.join(__dirname, "views"));
app.set("view engine", "jade");

app.use(logger("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cookieParser());
app.use(express.static(path.join(__dirname, "public")));

app.use(config.app.contextPath + "/download/project", projectsRouter);
app.use(config.app.contextPath + "/download/estimate", estimateRouter);
app.use(config.app.contextPath + "/download/musterRoll", musterRollRouter);
app.use(config.app.contextPath + "/download/workOrder", workOrderRouter);
app.use(config.app.contextPath + "/bill", groupBills);
app.use(config.app.contextPath + "/download/deviationStatement", deviationStatementRouter);
app.use(config.app.contextPath + "/download/measurementBook", measurementBookRouter);
app.use(config.app.contextPath + "/download/detailedEstimate", detailedEstimateRouter);
app.use(config.app.contextPath + "/download/detailedEstimate", detailedEstimateRouter);
app.use(config.app.contextPath + "/download/issueOfSummon", issueOfSummon);
app.use(config.app.contextPath + "/download/rescheduleRequestJudge", rescheduleRequestJudge);
app.use(config.app.contextPath + "/download/newHearingDateAfterReschedule", newHearingDateAfterReschedule);
app.use(config.app.contextPath + "/download/scheduleHearingDate", scheduleHearingDate);
app.use(config.app.contextPath + "/download/acceptReschedulingRequest", acceptReschedulingRequest);
app.use(config.app.contextPath + "/download/rejectReschedulingRequest", rejectReschedulingRequest);
app.use(config.app.contextPath + "/download/acceeptAdrApplication", acceeptAdrApplication);
app.use(config.app.contextPath + "/download/rejectAdrApplication", rejectAdrApplication);

// catch 404 and forward to error handler
app.use(function (req, res, next) {
  next(createError(404));
});

// error handler
app.use(function (err, req, res, next) {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get("env") === "development" ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render("error");
});
// Commenting consumer listener becuase excel bill gen is not required. IFMS adapter will process the payment.
// listenConsumer();
module.exports = app;
