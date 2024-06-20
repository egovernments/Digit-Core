import React from "react";
import { AltRoute } from "./AltRoute";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "AltRoute",
  component: AltRoute,
};

export const Default = () => <AltRoute />;
export const Fill = () => <AltRoute fill="blue" />;
export const Size = () => <AltRoute height="50" width="50" />;
export const CustomStyle = () => <AltRoute style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <AltRoute className="custom-class" />;
export const Clickable = () => <AltRoute onClick={()=>console.log("clicked")} />;

const Template = (args) => <AltRoute {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
