import React, { Children } from "react";
import DisplayPhotos from "../DisplayPhotos";

export default {
  title: "Atoms/DisplayPhotos",
  component: DisplayPhotos,
};

const Template = (args) => <DisplayPhotos {...args} />;

export const PlayGround = Template.bind({});
PlayGround.args = {
  srcs: [
    "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
    "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
    "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf",
  ],
  className: "custom",
  style: {},
};

export const Primary = Template.bind({});
Primary.args = {
  srcs: ["https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"],
};
