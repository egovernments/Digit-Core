import React from "react";
import { GetApp } from "./GetApp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "GetApp",
  component: GetApp,
};

export const Default = () => <GetApp />;
export const Fill = () => <GetApp fill="blue" />;
export const Size = () => <GetApp height="50" width="50" />;
export const CustomStyle = () => <GetApp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <GetApp className="custom-class" />;

export const Clickable = () => <GetApp onClick={()=>console.log("clicked")} />;

const Template = (args) => <GetApp {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
