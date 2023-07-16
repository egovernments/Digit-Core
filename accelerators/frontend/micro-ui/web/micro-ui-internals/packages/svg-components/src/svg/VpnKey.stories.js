import React from "react";
import { VpnKey } from "./VpnKey";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "VpnKey",
  component: VpnKey,
};

export const Default = () => <VpnKey />;
export const Fill = () => <VpnKey fill="blue" />;
export const Size = () => <VpnKey height="50" width="50" />;
export const CustomStyle = () => <VpnKey style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VpnKey className="custom-class" />;
export const Clickable = () => <VpnKey onClick={()=>console.log("clicked")} />;

const Template = (args) => <VpnKey {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
