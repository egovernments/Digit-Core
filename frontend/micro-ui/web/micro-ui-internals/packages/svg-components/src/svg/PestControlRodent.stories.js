import React from "react";
import { PestControlRodent } from "./PestControlRodent";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "PestControlRodent",
  component: PestControlRodent,
};

export const Default = () => <PestControlRodent />;
export const Fill = () => <PestControlRodent fill="blue" />;
export const Size = () => <PestControlRodent height="50" width="50" />;
export const CustomStyle = () => <PestControlRodent style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <PestControlRodent className="custom-class" />;

export const Clickable = () => <PestControlRodent onClick={()=>console.log("clicked")} />;

const Template = (args) => <PestControlRodent {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
