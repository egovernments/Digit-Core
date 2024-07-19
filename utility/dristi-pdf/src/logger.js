const { createLogger, format, transports } = require("winston");


const myFormat = format.printf(({ level, message, label, timestamp }) => {
  return `${timestamp} [${label}] [${level}]: ${message}`;
});

const logger = createLogger({
  format: format.combine(
    format.label({ label: 'BFF' }),
    format.timestamp({ format: " YYYY-MM-DD HH:mm:ss.SSSZZ " }),
    format.simple(),
    myFormat
  ),
  transports: [new transports.Console()],
});

//export default logger;
module.exports = { logger };