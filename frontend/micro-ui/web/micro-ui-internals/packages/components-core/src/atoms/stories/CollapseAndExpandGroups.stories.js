import React, { Children } from "react";
import CollapseAndExpandGroups from "../CollapseAndExpandGroups";

export default {
  title: "Atoms/CollapseAndExpandGroups",
  component: CollapseAndExpandGroups,
};

const Template = (args) => <CollapseAndExpandGroups {...args} />;

export const PlayGround = Template.bind({});
PlayGround.args = {
  children: <p>sdjkhfsdjkfhjkshfjksdhkjfgsdjhsfgdjfsdgjsdfgjhdfgjhdgsjhgjhgsdjhkfjksdfklsjflkflkasjfklhsdlkh</p>,
  groupElements: true,
  groupHeader: "",
  headerLabel: "Lorem",
  headerValue: "Lorem Ipsum",
  customClass: "sdklfhasdkhfjsdasdajkhfsdhfhjkashjhksdf",
};

export const Primary = Template.bind({});
Primary.args = {
  children: <p>askhfasj kjasdhjasdh kjsafhsdja</p>,
  groupElements: true,
  groupHeader: "",
  headerLabel: "Lorem",
  headerValue: "Lorem Ipsum",
  customClass: "sdklfhasdkhfjsdasdajkhfsdhfhjkashjhksdf",
};
