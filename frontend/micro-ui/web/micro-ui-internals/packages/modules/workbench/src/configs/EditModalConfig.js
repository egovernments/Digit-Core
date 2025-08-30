import { Dropdown, Loader } from '@egovernments/digit-ui-react-components';
import React, { useState } from 'react'

const getEditModalConfig = ({
    t,
    editRow,
    formData
}) => {
  
    return {
        label: {
            heading: `WBH_LOC_EDIT_LOCALISATION`,
            submit: `WBH_LOC_EDIT_MODAL_SUBMIT`,
            cancel: "WBH_LOC_EDIT_MODAL_CANCEL",
        },
        form: [
            {
                body: [
                  {
                    label: t("WBH_LOC_MODULE"),
                    type: "text",
                    disable:true,
                    populators: {
                        name: "module",
                        defaultValue:editRow.module,
                    },
                  },
                  {
                    label: t("WBH_LOC_LOCALE"),
                    type: "text",
                    disable:true,
                    populators: {
                        name: "locale",
                        defaultValue:editRow.originalLocale || formData?.searchForm?.locale?.value,
                    },
                  },
                  {
                    label: t("WBH_LOC_CODE"),
                    type: "text",
                    disable:true,
                    populators: {
                        name: "code",
                        defaultValue:editRow.code,
                    },
                  },
                  {
                    label: t("WBH_LOC_MESSAGE_VALUE"),
                    type: "text",
                    disable:false,
                    populators: {
                        name: "message",
                        // defaultValue:editRow.defaultMessage,
                    },
                  }
                ]
            }
        ]
    }
}

export default getEditModalConfig