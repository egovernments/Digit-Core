import React from "react";
import { SwitchRight } from "./SwitchRight";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "SwitchRight",
  component: SwitchRight,
};

export const Default = () => <SwitchRight />;
export const Fill = () => <SwitchRight fill="blue" />;
export const Size = () => <SwitchRight height="50" width="50" />;
export const CustomStyle = () => <SwitchRight style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <SwitchRight className="custom-class" />;
export const Clickable = () => <SwitchRight onClick={()=>console.log("clicked")} />;

const Template = (args) => <SwitchRight {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
