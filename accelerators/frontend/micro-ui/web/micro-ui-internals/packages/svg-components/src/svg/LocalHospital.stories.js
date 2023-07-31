import React from "react";
import { LocalHospital } from "./LocalHospital";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "LocalHospital",
  component: LocalHospital,
};

export const Default = () => <LocalHospital />;
export const Fill = () => <LocalHospital fill="blue" />;
export const Size = () => <LocalHospital height="50" width="50" />;
export const CustomStyle = () => <LocalHospital style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <LocalHospital className="custom-class" />;

export const Clickable = () => <LocalHospital onClick={()=>console.log("clicked")} />;

const Template = (args) => <LocalHospital {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
