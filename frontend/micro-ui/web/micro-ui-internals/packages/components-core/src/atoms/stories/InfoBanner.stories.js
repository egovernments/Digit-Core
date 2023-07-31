import React from "react";
import InfoBanner from "../InfoBanner";

export default {
  title: "Atoms/InfoBanner",
  component: InfoBanner,
};

const Template = (args) => <InfoBanner {...args} />;

export const PlayGround = Template.bind({});
PlayGround.args = {
  label: "a default info message",
  text: "",
};

export const Primary = Template.bind({});
Primary.args = {
  label: "a default info message",
  text: "",
};
