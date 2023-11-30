import { StoreService } from "./Store/service";

const StoreData = {
  getInitData: () => StoreService.getInitData(),
  getCurrentLanguage: () => Digit.SessionStorage.get("locale") || Digit.Utils.getDefaultLanguage(),
};

export default StoreData;
