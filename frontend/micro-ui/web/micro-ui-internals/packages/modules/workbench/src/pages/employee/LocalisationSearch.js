import { AddFilled, Button, Header, InboxSearchComposer, Loader, Dropdown,Toast,WorkflowModal,ActionBar,SubmitBar } from "@egovernments/digit-ui-react-components";
import React, { useState, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import _, { drop } from "lodash";
import { Config } from "../../configs/LocalisationSearchConfig";
import getEditModalConfig from "../../configs/EditModalConfig";
import { useQueryClient } from "react-query";

const LocalisationSearch = () => {
  const { t } = useTranslation();
  const queryClient = useQueryClient()
  const history = useHistory();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  const [showToast, setShowToast] = useState(false);
  const [modalConfig, setModalConfig] = useState(null);
  const [showModal, setShowModal] = useState(false);
  const [editRow,setEditRow] = useState(null)
  const [formData,setFormData] = useState(null)
  const [callRefetch,setCallRefetch] = useState(false)
  const reqCriteriaAdd = {
    url: `/localization/messages/v1/_upsert`,
    params: {},
    body: {
      tenantId,
    },
    config: {
      enabled: true,
    },
  };

  const mutation = Digit.Hooks.useCustomAPIMutationHook(reqCriteriaAdd);

  const closeToast = () => {
    setTimeout(() => {
      setShowToast(null);
    }, 5000);
  };

  const formUpdate = (form) => {
    setFormData(form)
  }

  const onModalSubmit = async (payload) => {
    if(!payload?.message){
      setShowToast({ label: `${t("WBH_LOC_ENTER_VALID_MESSAGE")}`,isError: true,style:{
        zIndex:"10000"
      } });
      closeToast()
      return
    }
    const onSuccess = (resp) => {
      setShowToast({ label: `${t("WBH_LOC_UPDATE_SUCCESS")}` });
      setShowModal(null)
      setEditRow(null)
      setModalConfig(null)
      // const queryCache = queryClient.getQueryCache()
      // const queryKeys = queryCache.getAll().map(cache => cache.queryKey) // QueryKey[]
      // queryClient.invalidateQueries([`/localization/messages/v1/_upsert`,`Random`])
      // queryClient.invalidateQueries([`/localization/messages/v1/_upsert`,`Random`,`defaultLocale`])
      closeToast();
      setCallRefetch(true)
    };
    const onError = (resp) => {
      let label = `${t("WBH_LOC_UPDATE_FAIL")}: `
      resp?.response?.data?.Errors?.map((err,idx) => {
        if(idx===resp?.response?.data?.Errors?.length-1){
          label = label + t(Digit.Utils.locale.getTransformedLocale(err?.code)) + '.'
        }else{
        label = label + t(Digit.Utils.locale.getTransformedLocale(err?.code)) + ', '
        }
      })
      setShowToast({ label, isError: true });
      setShowModal(null)
      setEditRow(null)
      closeToast();
    };


    mutation.mutate(
      {
        params: {},
        body: {
          tenantId,
          messages: [payload],
        },
      },
      {
        onError,
        onSuccess,
      }
    );

  }

  const onClickSvg = (row) => {
    setEditRow(row.original)
    setShowModal(true)
  }
  
  useEffect(() => {
    if(editRow) {
      setModalConfig(
        getEditModalConfig({
          t,
          editRow,
          formData
        })
      );
    }
  }, [editRow]);

  return (
    <React.Fragment>
      <div className="jk-header-btn-wrapper">
      <Header className="works-header-search">{t(Config?.label)}</Header>
      {/* {Config && Digit.Utils.didEmployeeHasRole(Config?.actionRole) && (
          <Button
            label={t(Config?.actionLabel)}
            variation="secondary"
            icon={<AddFilled style={{ height: "20px", width: "20px" }} />}
            onButtonClick={() => {
              history.push(`/${window?.contextPath}/employee/${Config?.actionLink}`);
            }}
            type="button"
            className={'header-btn'}
          />
        )} */}
      {
        Config && Digit.Utils.didEmployeeHasRole(Config?.actionRole) &&
        <ActionBar >
          <SubmitBar disabled={false} onSubmit={() => {
              history.push(`/${window?.contextPath}/employee/${Config?.actionLink}`);
            }} label={t("WBH_ADD_LOCALISATION")} />
        </ActionBar>
      }
      </div>
      {Config && <div className="inbox-search-wrapper">
        <InboxSearchComposer onFormValueChange={formUpdate} configs={Config} additionalConfig = {{
          resultsTable:{
            onClickSvg
          },
          search:{
            callRefetch,
            setCallRefetch
          }
        }}></InboxSearchComposer>
      </div>}
      {showModal && modalConfig && <WorkflowModal closeModal={() => setShowModal(false)} onSubmit={onModalSubmit} config={modalConfig} popupModuleActionBarStyles={{marginTop:"-1rem"}} popupModuleMianStyles={{marginTop:"-2rem"}} />}
      {showToast && <Toast label={showToast.label} error={showToast?.isError} isDleteBtn={true} onClose={()=>setShowToast(null)} style={showToast?.style}></Toast>}
    </React.Fragment>
  );
};

export default LocalisationSearch;
