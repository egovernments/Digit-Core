import React from "react";
import { useTranslation } from "react-i18next";
import { Loader } from "@egovernments/digit-ui-react-components"

const Header = ({styles}) => {
  const { data: storeData, isLoading } = Digit.Hooks.useStore.getInitData();
  const { stateInfo } = storeData || {};
  const { t } = useTranslation()

  if (isLoading) return <Loader/>;

  return (
    <div className="bannerHeader" style={styles ? styles : ""}>
      <img className="bannerLogo" src={stateInfo?.logoUrl} />
      <p>{t(`TENANT_TENANTS_${stateInfo?.code.toUpperCase()}`)}</p>
    </div>
  );
}

export default Header;