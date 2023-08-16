import { useInitStore } from "./store";
import useAccessControl from "./useAccessControl";
import useClickOutside from "./useClickOutside";
import useCustomMDMS from "./useCustomMDMS";
import useDocumentSearch from "./useDocumentSearch";
import useDynamicData from "./useDynamicData";
import useLocation from "./useLocation";
import useBoundaryLocalities from "./useLocalities";
import useCommonMDMS from "./useMDMS";
import useWorkflowDetailsV2 from "./useWorkflowDetailsV2";
import useModuleTenants from "./useModuleTenants";
import useQueryParams from "./useQueryParams";
import useRouteSubscription from "./useRouteSubscription";
import { useUserSearch } from "./userSearch";
import useSessionStorage from "./useSessionStorage";
import useApplicationStatusGeneral from "./useStatusGeneral";
import useStore from "./useStore";
import { useTenants } from "./useTenants";
import useWorkflowDetails from "./workflow";
import useCustomAPIHook from "./useCustomAPIHook";
import useUpdateCustom from "./useUpdateCustom";
import useGenderMDMS from "./useGenderMDMS";
import useEmployeeSearch from "./useEmployeeSearch";
import useGetHowItWorksJSON from "./useHowItWorksJSON";
import { usePrivacyContext } from "./usePrivacyContext";
import useStaticData from "./useStaticData";

const Hooks = {
  useSessionStorage,
  useQueryParams,
  useWorkflowDetails,
  useInitStore,
  useClickOutside,
  useUserSearch,
  useEmployeeSearch,
  useBoundaryLocalities,
  useCommonMDMS,
  useApplicationStatusGeneral,
  useModuleTenants,
  useStore,
  useDocumentSearch,
  useTenants,
  useAccessControl,
  usePrivacyContext,
  useGenderMDMS,
  useRouteSubscription,
  useCustomAPIHook,
  useWorkflowDetailsV2,
  useUpdateCustom,
  useCustomMDMS,
  useGetHowItWorksJSON,
  useStaticData,
  useDynamicData,
  useLocation,
};

export default Hooks;
