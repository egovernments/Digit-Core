import { useInitStore } from "./store";
import useClickOutside from "./useClickOutside";
import useCustomMDMS from "./useCustomMDMS";
import useLocation from "./useLocation";
import useBoundaryLocalities from "./useLocalities";
import useCommonMDMS from "./useMDMS";
import useModuleTenants from "./useModuleTenants";
import useQueryParams from "./useQueryParams";
import useRouteSubscription from "./useRouteSubscription";
import useSessionStorage from "./useSessionStorage";
import useStore from "./useStore";
import { useTenants } from "./useTenants";
import useCustomAPIHook from "./useCustomAPIHook";
import { usePrivacyContext } from "./usePrivacyContext";
import useStaticData from "./useStaticData";

const Hooks = {
  useSessionStorage,
  useQueryParams,
  useInitStore,
  useClickOutside,
  useBoundaryLocalities,
  useCommonMDMS,
  useModuleTenants,
  useStore,
  useTenants,
  usePrivacyContext,
  useRouteSubscription,
  useCustomAPIHook,
  useCustomMDMS,
  useStaticData,
  useLocation,
};

export default Hooks;
