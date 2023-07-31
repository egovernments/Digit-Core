import React from "react";
import { Http } from "./Http";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Http",
  component: Http,
};

export const Default = () => <Http />;
export const Fill = () => <Http fill="blue" />;
export const Size = () => <Http height="50" width="50" />;
export const CustomStyle = () => <Http style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Http className="custom-class" />;

export const Clickable = () => <Http onClick={()=>console.log("clicked")} />;

const Template = (args) => <Http {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
