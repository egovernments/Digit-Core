import React from "react";
import { AppRegistration } from "./AppRegistration";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AppRegistration",
  component: AppRegistration,
};

export const Default = () => <AppRegistration />;
export const Fill = () => <AppRegistration fill="blue" />;
export const Size = () => <AppRegistration height="50" width="50" />;
export const CustomStyle = () => <AppRegistration style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AppRegistration className="custom-class" />;
export const Clickable = () => <AppRegistration onClick={()=>console.log("clicked")} />;

const Template = (args) => <AppRegistration {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
