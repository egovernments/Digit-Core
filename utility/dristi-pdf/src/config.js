// config.js
// const env = process.env.NODE_ENV; // 'dev' or 'test'

HOST = process.env.EGOV_HOST || "localhost";


if (!HOST) {
  console.log("You need to set the HOST variable");
  process.exit(1);
}

module.exports = {
  auth_token: process.env.AUTH_TOKEN,

  KAFKA_BROKER_HOST: process.env.KAFKA_BROKER_HOST || "localhost:9092",
  KAFKA_RECEIVE_CREATE_JOB_TOPIC: process.env.KAFKA_RECEIVE_CREATE_JOB_TOPIC || "PDF_GEN_RECEIVE",
  KAFKA_BULK_PDF_TOPIC: process.env.KAFKA_BULK_PDF_TOPIC || "BULK_PDF_GEN",
  KAFKA_PAYMENT_EXCEL_GEN_TOPIC: process.env.KAFKA_PAYMENT_EXCEL_GEN_TOPIC || "PAYMENT_EXCEL_GEN",
  KAFKA_EXPENSE_PAYMENT_CREATE_TOPIC: process.env.KAFKA_EXPENSE_PAYMENT_CREATE_TOPIC || "expense-payment-create",

  PDF_BATCH_SIZE: process.env.PDF_BATCH_SIZE || 40,

  DB_USER: process.env.DB_USER || "postgres",
  DB_PASSWORD: process.env.DB_PASSWORD || "postgres",
  DB_HOST: process.env.DB_HOST || "localhost",
  DB_NAME: process.env.DB_NAME || "postgres",
  DB_PORT: process.env.DB_PORT || 5432,

  pdf: {
    issue_of_summon:"summons-issue",
    reschedule_request_judge:"reschedule-request-judge",
    new_hearing_date_after_reschedule: "new-hearing-date-after-rescheduling",
    schedule_hearing_date: "schedule-hearing-date",
    accept_rescheduling_request: "accept-reschedule-request",
    reject_rescheduling_request:"reject-reschedule-request",
    accept_adr_application : "accept-adr-application",
    reject_adr_application: "reject-adr-application"
  },

  app: {
    port: parseInt(process.env.APP_PORT) || 8080,
    host: HOST,
    contextPath: process.env.CONTEXT_PATH || "/egov-pdf",
  },

  host: {
    mdms: process.env.EGOV_MDMS_HOST || 'http://localhost:8081',
    pdf: process.env.EGOV_PDF_HOST || 'http://localhost:8070',
    user: process.env.EGOV_USER_HOST || HOST,
    case:'http://localhost:8091',
    order: 'http://localhost:8092',
    hrms:'http://localhost:8082',
    individual: 'http://localhost:8085',
    hearing: 'http://localhost:8093',
    workflow: process.env.EGOV_WORKFLOW_HOST || HOST,
    projectDetails: process.env.EGOV_PROJECT_HOST || 'http://localhost:8081/',
    estimates: process.env.EGOV_ESTIMATE_HOST || 'http://localhost:8084/',
    musterRoll: process.env.EGOV_MUSTER_ROLL_HOST || 'http://localhost:8085',
    contract: process.env.EGOV_CONTRACT_HOST || 'http://localhost:8086',
    organisation: process.env.EGOV_ORGANISATION_HOST || 'http://localhost:8087',
    localization: process.env.EGOV_LOCALIZATION_HOST || 'http://localhost:8083',
    expense: process.env.EXPENSE_SERVICE_HOST || 'http://localhost:8090',
    bankaccount: process.env.BANKACCOUNT_SERVICE_HOST || 'http://localhost:8091',
    filestore: process.env.EGOV_FILESTORE_SERVICE_HOST || 'http://localhost:8084',
    expense_calculator: process.env.EXPENSE_CALCULATOR_SERVICE_HOST || 'http://localhost:8093',
    measurements: process.env.EGOV_MEASUREMENT_HOST || 'http://localhost:8099',
  },

  paths: {
    pdf_create: "/pdf-service/v1/_createnosave",
    case_search:"/case/v1/_search",
    order_search: "/order/v1/search",
    hearing_search: "/hearing/v1/search",
    hrms_search:"/egov-hrms/employees/_search",
    individual_search:"/individual/v1/_search",
    user_search: "/user/_search",
    mdms_search: "/egov-mdms-service/v2/_search",
    workflow_search: "/egov-workflow-v2/egov-wf/process/_search",
    projectDetails_search: "/project/v1/_search",
    estimate_search: "/estimate/v1/_search",
    musterRoll_search: "/muster-roll/v1/_search",
    contract_search: "/contract/v1/_search",
    mdms_get: "/egov-mdms-service/v1/_get",
    orgnisation_search: "/org-services/organisation/v1/_search",
    expense_bill_search: "/expense/bill/v1/_search",
    expense_payment_search: "/expense/payment/v1/_search",
    bankaccount_search: "/bankaccount-service/bankaccount/v1/_search",
    expense_calculator_estimate: "/expense-calculator/v1/_estimate",
    expense_calculator_search: "/expense-calculator/v1/_search",
    localization_search: "/localization/messages/v1/_search",
    deviationStatement_search: "/estimate/v1/_search",
    measurement_book_search: "/mukta-services/measurement/_search",
  },

  constraints: {
    "beneficiaryIdByHeadCode": "Deduction_{tanentId}_{headcode}"
  }
};
