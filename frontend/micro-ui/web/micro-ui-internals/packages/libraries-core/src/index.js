import i18next from "i18next";
import { useStore } from "./services/index";
import { initI18n } from "./translations/index";
import { Storage, PersistantStorage } from "./services/atoms/Utils/Storage";
import { LocationService } from "./services/elements/Location";
import { LocalityService } from "./services/elements/Localities";
import { LocalizationService } from "./services/elements/Localization/service";
import * as dateUtils from "./services/atoms/Utils/Date";
import { MdmsService } from "./services/elements/MDMS";
import { ULBService } from "./services/molecules/Ulb";
import { ComponentRegistryService } from "./services/elements/ComponentRegistry";
import StoreData from "./services/molecules/StoreData";
import Contexts from "./contexts";
import Hooks from "./hooks";
import Utils from "./utils";
import { CustomService } from "./services/elements/CustomService";

const setupLibraries = (Library, props) => {
  window.Digit = window.Digit || {};
  window.Digit[Library] = window.Digit[Library] || {};
  window.Digit[Library] = { ...window.Digit[Library], ...props };
};

const initCoreLibraries = () => {
  setupLibraries("SessionStorage", Storage);
  setupLibraries("PersistantStorage", PersistantStorage);
  setupLibraries("ULBService", ULBService);
  setupLibraries("Services", { useStore });
  setupLibraries("LocationService", LocationService);
  setupLibraries("LocalityService", LocalityService);
  setupLibraries("LocalizationService", LocalizationService);
  setupLibraries("DateUtils", dateUtils);
  setupLibraries("MDMSService", MdmsService);
  setupLibraries("CustomService", CustomService);
  setupLibraries("ComponentRegistryService", ComponentRegistryService);
  setupLibraries("StoreData", StoreData);
  setupLibraries("Contexts", Contexts);
  setupLibraries("Hooks", Hooks);
  setupLibraries("Customizations", {});
  setupLibraries("Utils", Utils);

  return new Promise((resolve) => {
    initI18n(resolve);
  });
};

export { initCoreLibraries, Hooks };
