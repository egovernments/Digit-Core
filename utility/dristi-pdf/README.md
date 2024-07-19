# works-pdf service

works-pdf service work in between pdf-service and client requesting pdfs. Earlier client used to directly call pdf-service with complete data as json, but with introduction of this new service one can provide just few parameters ex:- applicationnumber, tenantId to this new service to get a pdf. 
### DB UML Diagram

- NA

### Service Dependencies

- egov-mdms-service
- Pdf-service
- User-service
- Workflow-service
- Project-service
- Estimate-service
- MusterRoll-service
- contract-service


### Swagger API Contract

http://editor.swagger.io/?url=https://raw.githubusercontent.com/egovernments/DIGIT-Works/master/utilities/works-pdf/docs/works-pdf_contract.yml#!/

## Service Details

works-pdf service is new service being added which can work in between existing pdf-service and client requesting pdfs. Earlier client used to directly call pdf-service with complete data as json, but with introduction of this new service one can provide just few parameters ex:- applicationnumber, tenantId to this new service to get a pdf. The works-pdf service will take responsibility of getting application data from concerned service and also will do any enrichment if required and then with the data call pdf service to get pdf directly . The service will return pdf binary as response which can be directly downloaded by the client. With this service the existing pdf service endpoints need not be exposed to frontend.

For any new pdf requirement one new endpoint with validations and logic for getting data for pdf has to be added in the code. With separate endpoint for each pdf we can define access rules per pdf basis. Currently works-pdf service has endpoint for following pdfs used in our system:-

- ProjectDetail pdf
- Estimate pdf
- Nominal Muster Roll pdf
- Workorder pdf


#### Configurations

**Steps/guidelines for adding support for new pdf:**

- Make sure the config for pdf is added in the PDF-Service.Refer the PDF service [documentatiom](https://digit-discuss.atlassian.net/l/c/f3APeZPF )

- Follow code of [existing supported PDFs](https://github.com/egovernments/DIGIT-Works/tree/master/utilities/works-pdf/src/routes) and create new endpoint with suitable search parameters for each PDF

- Put parameters validations, module level validations ex:- application status,applicationtype and api error responses with proper error messages and error codes

- Make sure whatever service is used for preparing data for PDF, search call to them by citizen returns citizens own record only, if not then adjust searchcriteria for them by including citizen mobilenumber or uuid to restrict citizen to create pdfs for his record only. If in the requirement itself it is explained that citizen can get PDF for others records also ex:- billgenie bill PDFs then no need for this check

- Prepare data for pdf by calling required services.

- Use correct pdf key with data to call and return PDF(use “/creatnosave” endpoint of PDF service)

- Add access to endpoint in MDMS for suitable roles

### API Details
Currently below endpoints are in use for ‘CITIZEN' and 'EMPLOYEE’ roles

| Endpoint | module | query parameter | Restrict Citizen to own records |
| -------- | ------ | --------------- | ------------------------------- |
|`/egov-pdf/download/project/project-details` | project | `projectId, tenantId` | false |
|`/egov-pdf/download/estimate/estimates`| estimate | `tenantId` , `estimateNumber` | false | 
|`/egov-pdf/download/musterRoll/muster-roll` | musterRoll | `musterRollNumber, tenantId` | false |
|`/egov-pdf/download/workOrder/work-order` | workOrder | `contractId, tenantId` | false |


### Kafka Consumers
NA

### Kafka Producers
NA
