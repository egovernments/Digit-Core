import { useInitStore } from "./store";
import useClickOutside from "./useClickOutside";
import useCustomMDMS from "./useCustomMDMS";
import useLocation from "./useLocation";
import useBoundaryLocalities from "./useLocalities";
import useCommonMDMS from "./useMDMS";
import useQueryParams from "./useQueryParams";
import useRouteSubscription from "./useRouteSubscription";
import useStore from "./useStore";
import { useTenants } from "./useTenants";
import useCustomAPIHook from "./useCustomAPIHook";

const Hooks = {
  useQueryParams,
  useInitStore,
  useClickOutside,
  useBoundaryLocalities,
  useCommonMDMS,
  useStore,
  useTenants,
  useRouteSubscription,
  useCustomAPIHook,
  useCustomMDMS,
  useLocation,
};

export default Hooks;
