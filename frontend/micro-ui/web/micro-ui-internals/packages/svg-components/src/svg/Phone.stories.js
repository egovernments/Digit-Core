import React from "react";
import { Phone } from "./Phone";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Phone",
  component: Phone,
};

export const Default = () => <Phone />;
export const Fill = () => <Phone fill="blue" />;
export const Size = () => <Phone height="50" width="50" />;
export const CustomStyle = () => <Phone style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Phone className="custom-class" />;

export const Clickable = () => <Phone onClick={()=>console.log("clicked")} />;

const Template = (args) => <Phone {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
