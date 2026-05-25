
## Terms of Service (ToS) for DIGIT LTS
This document outlines the principles and practices for long-term support of DIGIT as an open-source platform. It is not a commercial support agreement.
#### 1. Introduction
Digital Infrastructure for Governance and Inclusive Transformation (DIGIT) is an open-source, scalable, interoperable platform for responsive public service delivery and good governance.  It enables government agencies to digitize public service delivery  - providing unified interfaces for citizens, front-line employees and administrators to exchange information with each other in a seamless and trusted manner. DIGIT is an open-source platform built for scale. DIGIT is multi-tenant and can enable the digital transformation of multiple government agencies at speed and scale using a common shared infrastructure. eGov Foundation is the majority contributor and maintainer of DIGIT.
DIGIT 2.9 represents the most recent Long-Term Support (LTS) version, offering a stable and reliable foundation  for governments and their partners to build robust public service delivery systems.This version emphasizes enhanced security measures, improved system stability, streamlined deployment processes, simplified configuration, and comprehensive documentation. Periodic updates, encompassing both minor adjustments and significant enhancements, will be released as necessary. More information is available at https://core.digit.org/platform/releases/digit-2.9-lts 
#### 2. License: 
DIGIT 2.9 LTS is licensed under the MIT license. It is a short and simple permissive license with conditions only requiring preservation of copyright and license notices. 
#### 3. Support and Maintenance for DIGIT 2.9 LTS Core services
Note: This document lays out the Terms of Service to support DIGIT 2.9 LTS Core only. This does not include domain Services built on DIGIT eg: DIGIT Urban Services or Health Campaigns etc. 
#### Support Services: 
3.1 Users are requested to use the ‘Guides’ available on https://core.digit.org/guides/installation-guide to install and start using DIGIT LTS. Documentation will be updated at regular intervals based on feedback from users. Inputs to the documentation can be submitted at <https://github.com/egovernments/Digit-Core/discussions> with the tag ‘ documentation’. 
3.2 As the main contributor and maintainer of DIGIT, eGov strives to ensure the bugs/ issues/ enhancements reported by the community are attended to on a best effort basis. All users are requested to contribute to the discussion board as per their capacity. 

3.3 Process for issue Reporting: 
*   First, the user/ partner should check the GitHub core repo for the same/ similar issue already reported and the fixes/ workaround provided.

*   If the above is not available, the user/ partner shall provide details of investigations carried out by their teams along with logs and inferences leading them to believe that the issue lies with the core services of the DIGIT LTS. https://github.com/egovernments/Digit-Core/discussions is the single point for all discussions which require support. Please ensure use of appropriate tags( if any) before submitting a new issue. 

3.4 eGov support team shall acknowledge the discussion board input and analyse it. If it qualifies as an issue, it will be moved to the issue list for the users to track there. Support team will check versions to ensure that the services running are supported by the LTS version.

3.5 Response SLAs: Hours of support: 9 am to 6 pm Indian Standard Time Monday to Friday (excluding public holidays)

| Severity                  | Description                                                                                                                                                                      | First Response SLA |
|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------|
| P1: Production Halted     | Critical bug impacting service for which the cause is unknown. No bypass or workaround available. Typically, security bugs will be top priority and taken up in this category.   | 1 working day     |
| P2: Production Degraded   | A key component of the application is degraded, unusable or unavailable. User has isolated it to an issue in the core services. No workaround available.                         | 2 working days    |
| P3: Business Unaffected   | A component of the application is degraded, which causes a minor inconvenience, but a workaround is available.                                                                    | 4 working days    |


* \**First Response SLA is defined as the committed time frame within which the support team acknowledges a reported issue after receiving any additional clarifications from the user and initiates investigation or action
Note: Security vulnerabilities should be reported privately using the Security tab in the repository. Please do not disclose them in public issues or discussions.*

3.6 Wherever feasible, eGov support team shall provide a resolution to the issue by issuing:

1.   For P1 issues, eGov will provide a patch release to close out the issue. The timeline for patch release will be dependent on the estimate of work required and will be communicated to the users. Such fixes will be added to the backlog to be considered for DIGIT LTS roadmap in the next iterations after following the regular QA processes. 

2.   For others, clarification, or workaround to resolve the issue. In this case, eGov support team will provide troubleshooting support to the user/ partner who has time critical dependency on resolving the issues and users are encouraged to contribute this back to the platform. 

3.   If this is found to be a genuine bug in the core services which is already on the platform roadmap, eGov will plan to fix it in a newer release and the user/Partner will get the fix once upgraded by eGov.


4. Support will be limited to the core services of the platform. Please refer to the list of services- https://core.digit.org/platform/core-services. Elements like UI Frameworks are available for users but not supported. In case of any changes to the core services by the user/partner, then those services and its dependent services will no longer be supported. However, users are encouraged to report any issues on the discussion board for potential resolution by the DIGIT community.


*Please note that all issues will be responded to but not attended by eGov Foundation alone and will be resolved on a best effort basis. We aim to anchor a community around DIGIT which over time will enable resolution by different members of the community. We request users from organisations signed up on the Partner Program to please mention the same for priority response.*

4. Maintenance and Updates: 
4.1 Any Known issues in the LTS would be documented on GitHub in the core platform repo so that users can check for fixes/work-arounds. 
4.2 eGov will share the DIGIT roadmap with users every quarter. The roadmap will provide enough detail that allows the Partner to plan for changes at their end. All issues on the discussion board will be taken into consideration while finalizing the platform roadmap. The platform roadmap will be published on Gitbook with a discussion board for any Q&A.  
4.3 Any user in the community is allowed to raise a pull request on the DIGIT core repository. At present, the core team at eGov is responsible for maintaining the core repository and ensuring the timely review and resolution of pull requests.

#### 5. Duration and End-of-Life (EOL)
*   5.1 Support Period: Support for this iteration of DIGIT (2.9 LTS)  will be available for the five years (till 2029), with a clear migration guide available to facilitate the transition to subsequent LTS versions once the current support period concludes.
*   5.2 End-of-Life Policy: End of Service Life (EOSL) considerations for the LTS version will be shared by eGov. EOSL will be flagged as per the upgrade guideline. This will also act as a trigger for upgrades. Notice will be provided atleast 6 months in advance of EoL wherever possible.
#### 6. User Responsibilities
6.1 Security: Please refer to general and role based security and privacy guidelines for DIGIT- https://core.digit.org/guides/security-and-privacy-guide
I. eGov will share the security audit reports carried out on a periodic basis, as and when these security audits are conducted.
Ii. Users/ partners are responsible for maintaining security, such as applying updates in a timely manner and securing their environment against threats.

#### 7. Disclaimers and Limitations of Liability
7.1 **Disclaimers**: As the primary contributor and maintainer of DIGIT, eGov will publish the latest reliability, accuracy and performance benchmarks for DIGIT LTS as and when they are done. However, we request all users to benchmark the parameters themselves and report in case of significant deviations. In such cases, eGov will work with the user to check for possible issues but does not guarantee the exact same performance.  
7.2 **Liability Limitations**: Limits on the liability of the software provider for damages arising from the use of the LTS release, including indirect or consequential damages.
Under no circumstance shall eGov or any of its employees, consultants, trustees, directors or agents be liable in any amount for special, incidental, consequential or indirect damages, loss of goodwill or profits, or exemplary or punitive damages, irrespective of who initiates the claim. eGov disclaims any and all liability in relation to the Platform and the services, if any, provided under this document

#### 8. Privacy and Data Protection
###### 8.1 Data Collection and Privacy Guidelines: 
DIGIT Platform team ensures key security and privacy features are incorporated in DIGIT, and provides guidelines for other actors. Detailed guidelines are available https://core.digit.org/guides/security-and-privacy-guide#digit-platform-practices and partners/ users are encouraged to follow these guidelines while developing/ implementing programs using DIGIT LTS.
###### 8.2 Modification of Terms: 
In the event of material changes to eGov’s financial or operational capacity, the terms of DIGIT Support may be revised with 60 days prior written notice.
###### 8.3 Acceptance of Changes:
Continued use of the LTS release by users/ Partners will constitute acceptance of any updated terms.
#### 9. Contact Information
Support Contact: All questions/ queries related to LTS release may be posted on the Github discussion link here.
#### 10. Additional Provisions
Third-Party Software: DIGIT core services are developed using the following technologies 
*   Java Spring Boot
*   Apache Kafka
*   Spring Cloud Gateway
*   Postgres
*   Elastic Search
*   Docker
*   Kubernetes
*   Terraform

*More information about these open-source tools and API gateways used in DIGIT are available at https://core.digit.org/platform/architecture/technology-architecture. DIGIT support will be subject to the support terms and conditions of these underlying technologies and some issues arising out of underlying issues in any of these technologies cannot be covered as part of this support document.* 
