import React from "react";
import { InfoBannerIcon } from "./InfoBannerIcon";

export default {
  tags: ["autodocs"],
  argTypes: {
    className: {
      options: ["custom-class"],
      control: { type: "check" },
    },
  },
  title: "InfoBannerIcon",
  component: InfoBannerIcon,
};

export const Default = () => <InfoBannerIcon />;
export const Fill = () => <InfoBannerIcon fill="blue" />;
export const Size = () => <InfoBannerIcon height="50" width="50" />;
export const CustomStyle = () => <InfoBannerIcon style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <InfoBannerIcon className="custom-class" />;

export const Clickable = () => <InfoBannerIcon onClick={() => console.log("clicked")} />;

const Template = (args) => <InfoBannerIcon {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" },
};
