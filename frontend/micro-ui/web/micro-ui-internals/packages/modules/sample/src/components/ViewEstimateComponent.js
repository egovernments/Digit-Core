import React, { Fragment, useState, useEffect, useRef, } from 'react'
import { Loader, WorkflowActions, WorkflowTimeline,ActionBar,Menu,SubmitBar, Toast } from '@egovernments/digit-ui-react-components';
import { useTranslation } from "react-i18next";
import ApplicationDetails from '../../../templates/ApplicationDetails';
import { useHistory } from 'react-router-dom';

const ViewEstimateComponent = ({editApplicationNumber,...props}) => {
    const [toast, setToast] = useState({show : false, label : "", error : false});
    const menuRef = useRef();
    const [isStateChanged, setStateChanged] = useState(``)
  
    const { t } = useTranslation()

    const { tenantId, estimateNumber } = Digit.Hooks.useQueryParams();
    const businessService = Digit?.Customizations?.["commonUiConfig"]?.getBusinessService("estimate");

    const { isLoading, data: applicationDetails, isError } = Digit.Hooks.estimates.useEstimateDetailsScreen(t, tenantId, estimateNumber,{}, isStateChanged)  
    
    useEffect(()=>{
        if(isError || (!isLoading && applicationDetails?.isNoDataFound)) {
            setToast({show : true, label : t("COMMON_ESTIMATE_NOT_FOUND"), error : true});
        }
    },[isLoading, isError, applicationDetails]);

    

    const handleToastClose = () => {
        setToast({show : false, label : "", error : false});
    }
    if (isLoading) return <Loader />
    return (
        <>
            {
                (!applicationDetails?.isNoDataFound) && !isError && 
                <>
                    <ApplicationDetails
                        applicationDetails={applicationDetails}
                        isLoading={isLoading}
                        applicationData={applicationDetails?.applicationData}
                        moduleCode="Estimate"
                        showTimeLine={true}
                        timelineStatusPrefix={`WF_${businessService}_`}
                        businessService={businessService}
                        // forcedActionPrefix={"ACTION_"}
                        tenantId={tenantId}
                        applicationNo={estimateNumber}
                        statusAttribute={"state"}
                    />
                    <WorkflowActions
                        forcedActionPrefix={`WF_${businessService}_ACTION`}
                        businessService={businessService}
                        applicationNo={estimateNumber}
                        tenantId={tenantId}
                        applicationDetails={applicationDetails?.applicationData}
                        // url={Digit.Utils.Urls.works.updateEstimate}
                        setStateChanged={setStateChanged}
                        moduleCode="Estimate"
                        // editApplicationNumber={editApplicationNumber}
                    />
                </>
                
            }
            {toast?.show && <Toast label={toast?.label} error={toast?.error} isDleteBtn={true} onClose={handleToastClose}></Toast>}
        </>
    )
}

export default ViewEstimateComponent