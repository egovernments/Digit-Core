import React from "react";
import { TouchApp } from "./TouchApp";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TouchApp",
  component: TouchApp,
};

export const Default = () => <TouchApp />;
export const Fill = () => <TouchApp fill="blue" />;
export const Size = () => <TouchApp height="50" width="50" />;
export const CustomStyle = () => <TouchApp style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TouchApp className="custom-class" />;
export const Clickable = () => <TouchApp onClick={()=>console.log("clicked")} />;

const Template = (args) => <TouchApp {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
